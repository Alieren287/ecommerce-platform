package com.alier.ecommerceproductservice.application.usecase;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.application.dto.BulkCreateProductRequest;
import com.alier.ecommerceproductservice.application.dto.CreateProductRequest;
import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.domain.service.ProductDomainService;
import com.alier.ecommerceproductservice.infrastructure.messaging.ProductEventPublisher;
import com.alier.ecommerceproductservice.infrastructure.search.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for creating a new product.
 */
@UseCase(description = "Create a new product")
@RequiredArgsConstructor
@Slf4j
@Service
public class CreateProductUseCase extends UseCaseHandler<CreateProductRequest, ProductDTO> {

    private final ProductRepository productRepository;
    private final ProductDomainService productDomainService;
    private final ProductEventPublisher eventPublisher;
    private final ProductSearchService searchService;

    /**
     * Validates that the product SKU is available
     */
    @Override
    protected void validate(CreateProductRequest request) throws BusinessException {
        if (productDomainService.isSkuExists(request.getSku())) {
            log.warn("Product SKU already exists: {}", request.getSku());
            throw new ProductException.ProductSkuAlreadyExistsException(request.getSku());
        }
    }

    /**
     * Creates a new product and publishes a domain event.
     */
    @Override
    @Transactional
    protected ProductDTO handle(CreateProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        Product product = createProductFromRequest(request);

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        // Index in Elasticsearch
        searchService.indexProduct(savedProduct);

        return ProductDTO.fromDomain(savedProduct);
    }

    /**
     * Creates multiple products in a single transaction
     *
     * @param request The bulk creation request containing a list of products to create
     * @return List of created product DTOs
     */
    @Transactional
    public List<ProductDTO> executeBulk(BulkCreateProductRequest request) {
        log.info("Bulk creating {} products", request.getProducts().size());

        List<ProductDTO> createdProducts = new ArrayList<>();
        List<String> existingSkus = new ArrayList<>();

        // First, validate all SKUs to ensure none are duplicates
        for (CreateProductRequest productRequest : request.getProducts()) {
            if (productDomainService.isSkuExists(productRequest.getSku())) {
                existingSkus.add(productRequest.getSku());
            }
        }

        // If any SKUs already exist, throw exception with all duplicates
        if (!existingSkus.isEmpty()) {
            log.warn("Found {} existing SKUs during bulk creation: {}", existingSkus.size(), existingSkus);
            throw new ProductException(
                    com.alier.ecommerceproductservice.domain.exception.ProductErrorCode.PRODUCT_SKU_EXISTS,
                    "The following SKUs already exist: " + String.join(", ", existingSkus)
            );
        }

        // Create all products
        List<Product> products = request.getProducts().stream()
                .map(this::createProductFromRequest)
                .collect(Collectors.toList());

        // Save all products in a batch
        List<Product> savedProducts = productRepository.saveAll(products);

        // Index all products in Elasticsearch
        searchService.indexProducts(savedProducts);

        // Convert to DTOs and publish events
        for (Product savedProduct : savedProducts) {
            // Convert to DTO
            ProductDTO productDTO = ProductDTO.fromDomain(savedProduct);
            createdProducts.add(productDTO);

            // Publish event (can be made asynchronous for better performance)
            eventPublisher.publishProductCreatedEvent(savedProduct);
        }

        log.info("Successfully created {} products in bulk", createdProducts.size());

        return createdProducts;
    }

    /**
     * Helper method to create a Product domain object from a CreateProductRequest
     *
     * @param request The request with product details
     * @return A new Product domain object
     */
    private Product createProductFromRequest(CreateProductRequest request) {
        Product product = Product.create(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getSku()
        );

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String imageUrl : request.getImageUrls()) {
                product.addImageUrl(imageUrl);
            }
        }

        return product;
    }
} 