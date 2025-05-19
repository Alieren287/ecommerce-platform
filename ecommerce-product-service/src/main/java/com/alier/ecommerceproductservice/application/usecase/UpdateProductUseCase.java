package com.alier.ecommerceproductservice.application.usecase;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.application.dto.BulkUpdateProductRequest;
import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import com.alier.ecommerceproductservice.application.dto.ProductPatchRequest;
import com.alier.ecommerceproductservice.application.dto.UpdateProductRequest;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.infrastructure.cache.ProductCacheService;
import com.alier.ecommerceproductservice.infrastructure.messaging.ProductEventPublisher;
import com.alier.ecommerceproductservice.infrastructure.search.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use case for updating an existing product.
 */
@UseCase(description = "Update an existing product")
@RequiredArgsConstructor
@Slf4j
@Service
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductEventPublisher eventPublisher;
    private final ProductCacheService cacheService;
    private final ProductSearchService searchService;

    /**
     * Updates an existing product.
     *
     * @param id      the ID of the product to update
     * @param request the update product request
     * @return the updated product DTO
     * @throws ProductException if the product is not found
     */
    @Transactional
    public ProductDTO execute(UUID id, UpdateProductRequest request) {
        return new UpdateProductUseCaseHandler().execute(new UpdateProductInput(id, request));
    }

    /**
     * Partially updates an existing product.
     *
     * @param id      the ID of the product to update
     * @param request the patch product request (with only the fields that need to be updated)
     * @return the updated product DTO
     * @throws ProductException if the product is not found
     */
    @Transactional
    public ProductDTO executePatch(UUID id, ProductPatchRequest request) {
        log.debug("Patching product with ID: {}", id);

        // Find the product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", id);
                    return new ProductException.ProductNotFoundException(id);
                });

        boolean updated = false;
        // Apply patch selectively based on non-null fields
        if (request.getName() != null) {
            product.update(
                    request.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStockQuantity()
            );
            updated = true;
        }

        if (request.getDescription() != null) {
            product.update(
                    product.getName(),
                    request.getDescription(),
                    product.getPrice(),
                    product.getStockQuantity()
            );
            updated = true;
        }

        if (request.getPrice() != null) {
            product.update(
                    product.getName(),
                    product.getDescription(),
                    request.getPrice(),
                    product.getStockQuantity()
            );
            updated = true;
        }

        if (request.getStockQuantity() != null) {
            product.update(
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    request.getStockQuantity()
            );
            updated = true;
        }

        if (request.getStatus() != null) {
            switch (request.getStatus()) {
                case ACTIVE -> product.activate();
                case INACTIVE -> product.deactivate();
                case DRAFT ->
                        log.warn("Cannot change product status back to DRAFT for product ID: {}. Status remains {}.", id, product.getStatus());
                // No default needed as ProductStatus is an enum
            }
            updated = true;
        }

        Product savedProduct;
        if (updated) {
            savedProduct = productRepository.save(product);
        } else {
            savedProduct = product; // No changes made, return original
        }


        // Update cache and search index
        ProductDTO productDTO = ProductDTO.fromDomain(savedProduct);
        cacheService.cacheProduct(productDTO);
        searchService.indexProduct(savedProduct);

        // Publish event only if there was an actual update
        if (updated) {
            eventPublisher.publishProductUpdatedEvent(savedProduct);
            log.info("Product {} patched successfully", id);
        } else {
            log.info("Product {} patch request received, but no changes were applied.", id);
        }


        return productDTO;
    }

    /**
     * Updates multiple products in a single transaction
     *
     * @param request The bulk update request containing product IDs and update data
     * @return List of updated product DTOs
     */
    @Transactional
    public List<ProductDTO> executeBulk(BulkUpdateProductRequest request) {
        log.info("Bulk updating {} products", request.getUpdates().size());

        List<ProductDTO> updatedProducts = new ArrayList<>();
        List<UUID> notFoundIds = new ArrayList<>();

        // First, retrieve all products to ensure they all exist
        List<UUID> productIds = request.getUpdates().stream()
                .map(BulkUpdateProductRequest.ProductUpdate::getId)
                .collect(Collectors.toList());

        List<Product> existingProducts = productRepository.findById(productIds);

        // Create a map of id -> product for easy lookup
        Map<UUID, Product> productMap = existingProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // Check if any products were not found
        for (UUID id : productIds) {
            if (!productMap.containsKey(id)) {
                notFoundIds.add(id);
            }
        }

        // If any products are not found, throw exception
        if (!notFoundIds.isEmpty()) {
            log.warn("Some products were not found during bulk update: {}", notFoundIds);
            throw new ProductException(
                    ProductErrorCode.PRODUCT_NOT_FOUND,
                    "The following products were not found: " + notFoundIds
            );
        }

        // Update each product
        List<Product> productsToUpdate = new ArrayList<>();

        for (BulkUpdateProductRequest.ProductUpdate update : request.getUpdates()) {
            Product product = productMap.get(update.getId());
            UpdateProductRequest updateRequest = update.getData();

            // Apply updates
            product = product.update(
                    updateRequest.getName(),
                    updateRequest.getDescription(),
                    updateRequest.getPrice(),
                    updateRequest.getStockQuantity()
                    // updateRequest.getImageUrl() removed
            );

            productsToUpdate.add(product);
        }

        // Save all products in a batch
        List<Product> savedProducts = productRepository.saveAll(productsToUpdate);

        // Update cache, search index, and publish events
        for (Product savedProduct : savedProducts) {
            ProductDTO productDTO = ProductDTO.fromDomain(savedProduct);
            updatedProducts.add(productDTO);

            // Update cache and search index
            cacheService.cacheProduct(productDTO);
            searchService.indexProduct(savedProduct);

            // Publish event
            eventPublisher.publishProductUpdatedEvent(savedProduct);
        }

        log.info("Successfully updated {} products in bulk", updatedProducts.size());

        return updatedProducts;
    }

    /**
     * Activates a product.
     *
     * @param id the ID of the product to activate
     * @return the activated product DTO
     * @throws ProductException if the product is not found or cannot be activated
     */
    @Transactional
    public ProductDTO activateProduct(UUID id) {
        log.info("Activating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", id);
                    return new ProductException.ProductNotFoundException(id);
                });

        Product activatedProduct = product.activate();
        Product savedProduct = productRepository.save(activatedProduct);

        // Update cache and search index
        ProductDTO productDTO = ProductDTO.fromDomain(savedProduct);
        cacheService.cacheProduct(productDTO);
        searchService.indexProduct(savedProduct);

        // Publish event
        eventPublisher.publishProductStatusChangedEvent(savedProduct, "ACTIVATED");

        return productDTO;
    }

    /**
     * Deactivates a product.
     *
     * @param id the ID of the product to deactivate
     * @return the deactivated product DTO
     * @throws ProductException if the product is not found
     */
    @Transactional
    public ProductDTO deactivateProduct(UUID id) {
        log.info("Deactivating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", id);
                    return new ProductException.ProductNotFoundException(id);
                });

        Product deactivatedProduct = product.deactivate();
        Product savedProduct = productRepository.save(deactivatedProduct);

        // Update cache and search index
        ProductDTO productDTO = ProductDTO.fromDomain(savedProduct);
        cacheService.cacheProduct(productDTO);
        searchService.indexProduct(savedProduct);

        // Publish event
        eventPublisher.publishProductStatusChangedEvent(savedProduct, "DEACTIVATED");

        return productDTO;
    }

    /**
     * Input data for the update product use case handler.
     */
    public record UpdateProductInput(UUID id, UpdateProductRequest request) {
    }

    /**
     * Inner handler class for updating a product.
     */
    private class UpdateProductUseCaseHandler extends UseCaseHandler<UpdateProductInput, ProductDTO> {

        @Override
        protected ProductDTO handle(UpdateProductInput input) throws BusinessException {
            log.debug("Updating product with ID: {}", input.id());

            Product product = productRepository.findById(input.id())
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", input.id());
                        return new ProductException.ProductNotFoundException(input.id());
                    });

            UpdateProductRequest request = input.request();

            Product updatedProduct = product.update(
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getStockQuantity()
                    // request.getImageUrl() removed
            );

            Product savedProduct = productRepository.save(updatedProduct);

            // Update cache and search index
            ProductDTO productDTO = ProductDTO.fromDomain(savedProduct);
            cacheService.cacheProduct(productDTO);
            searchService.indexProduct(savedProduct);

            // Publish event
            eventPublisher.publishProductUpdatedEvent(savedProduct);

            log.info("Product {} updated successfully", input.id());

            return productDTO;
        }
    }
} 