package com.alier.ecommerceproductservice.application.usecase;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.dto.PaginatedResponse;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import com.alier.ecommerceproductservice.application.dto.ProductFilterRequest;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.infrastructure.cache.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case for retrieving products.
 */
@UseCase(description = "Retrieve product information")
@RequiredArgsConstructor
@Slf4j
@Service
public class GetProductUseCase {

    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;

    /**
     * Gets a product by ID, using cache if available
     *
     * @param id the product ID
     * @return the product DTO
     * @throws BusinessException if the product is not found
     */
    @Transactional(readOnly = true)
    public ProductDTO getById(UUID id) {
        return new GetByIdUseCaseHandler().execute(id);
    }

    /**
     * Gets a product by SKU, using cache if available
     *
     * @param sku the product SKU
     * @return the product DTO
     * @throws ProductException if the product is not found
     */
    @Transactional(readOnly = true)
    public ProductDTO getBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);

        // Try to get from cache first
        return productCacheService.getProductBySku(sku)
                .orElseGet(() -> {
                    // If not in cache, get from repository
                    Product product = productRepository.findBySku(sku)
                            .orElseThrow(() -> {
                                log.warn("Product not found with ID: {}", sku);
                                return new ProductException.ProductNotFoundException(sku);
                            });

                    ProductDTO productDTO = ProductDTO.fromDomain(product);

                    // Store in cache for future requests
                    productCacheService.cacheProduct(productDTO);

                    return productDTO;
                });
    }

    /**
     * Gets all products
     *
     * @return list of product DTOs
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products");

        return productRepository.findAll().stream()
                .map(ProductDTO::fromDomain)
                .collect(Collectors.toList());
    }

    /**
     * Gets all products with pagination
     *
     * @param page    the page number (0-based)
     * @param size    the page size
     * @param sortBy  the field to sort by
     * @param sortDir the sort direction ("asc" or "desc")
     * @return paged response of product DTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductDTO> getAllProductsPaged(int page, int size, String sortBy, String sortDir) {
        log.debug("Fetching page {} of products, size {}, sortBy {}, sortDir {}", page, size, sortBy, sortDir);

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductDTO> content = productPage.getContent().stream()
                .map(ProductDTO::fromDomain)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                content,
                page,
                size,
                productPage.getTotalElements()
        );
    }

    /**
     * Gets products by status with pagination
     *
     * @param status  the product status
     * @param page    the page number (0-based)
     * @param size    the page size
     * @param sortBy  the field to sort by
     * @param sortDir the sort direction ("asc" or "desc")
     * @return paged response of product DTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductDTO> getProductsByStatus(ProductStatus status, int page, int size, String sortBy, String sortDir) {
        log.debug("Fetching page {} of products with status {}", page, status);

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByStatus(status, pageable);
        List<ProductDTO> content = productPage.getContent().stream()
                .map(ProductDTO::fromDomain)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                content,
                page,
                size,
                productPage.getTotalElements()
        );
    }

    /**
     * Searches for products with flexible filtering
     *
     * @param filterRequest the filter criteria
     * @param page          the page number (0-based)
     * @param size          the page size
     * @param sortBy        the field to sort by
     * @param sortDir       the sort direction ("asc" or "desc")
     * @return paged response of filtered product DTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductDTO> searchProducts(ProductFilterRequest filterRequest, int page, int size, String sortBy, String sortDir) {
        log.debug("Searching products with filters: {}", filterRequest);

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByFilters(filterRequest, pageable);
        List<ProductDTO> content = productPage.getContent().stream()
                .map(ProductDTO::fromDomain)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                content,
                page,
                size,
                productPage.getTotalElements()
        );
    }

    /**
     * Simple product search by name, description, or SKU
     *
     * @param searchTerm the search term
     * @param page       the page number (0-based)
     * @param size       the page size
     * @param sortBy     the field to sort by
     * @param sortDir    the sort direction ("asc" or "desc")
     * @return paged response of matching product DTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductDTO> simpleSearch(String searchTerm, int page, int size, String sortBy, String sortDir) {
        log.debug("Searching products for term: {}", searchTerm);

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.search(searchTerm, pageable);
        List<ProductDTO> content = productPage.getContent().stream()
                .map(ProductDTO::fromDomain)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                content,
                page,
                size,
                productPage.getTotalElements()
        );
    }

    /**
     * Gets products in a specific price range
     *
     * @param minPrice the minimum price (inclusive)
     * @param maxPrice the maximum price (inclusive)
     * @param page     the page number (0-based)
     * @param size     the page size
     * @param sortBy   the field to sort by
     * @param sortDir  the sort direction ("asc" or "desc")
     * @return paged response of product DTOs in the price range
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        log.debug("Fetching products with price between {} and {}", minPrice, maxPrice);

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        List<ProductDTO> content = productPage.getContent().stream()
                .map(ProductDTO::fromDomain)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                content,
                page,
                size,
                productPage.getTotalElements()
        );
    }

    /**
     * Creates a Sort object based on the given field and direction
     *
     * @param sortBy  the field to sort by (defaults to "id" if invalid)
     * @param sortDir the sort direction ("asc" or "desc")
     * @return Sort object
     */
    private Sort createSort(String sortBy, String sortDir) {
        // Set default sort field if empty or null
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "id";
        }

        // Validate sort field (only allow certain fields to be sorted on)
        if (!isValidSortField(sortBy)) {
            log.warn("Invalid sort field: {}. Using default 'id' instead.", sortBy);
            sortBy = "id";
        }

        // Set direction
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }

    /**
     * Check if the provided sort field is valid
     *
     * @param field field name
     * @return true if valid, false otherwise
     */
    private boolean isValidSortField(String field) {
        // List of allowed sort fields
        return switch (field) {
            case "id", "name", "price", "stockQuantity", "sku", "status", "createdAt", "updatedAt" -> true;
            default -> false;
        };
    }

    /**
     * Nested handler for getById operation
     */
    @RequiredArgsConstructor
    private class GetByIdUseCaseHandler extends UseCaseHandler<UUID, ProductDTO> {

        @Override
        protected ProductDTO handle(UUID id) throws BusinessException {
            log.debug("Fetching product with ID: {}", id);

            // Try to get from cache first
            return productCacheService.getProductById(id)
                    .orElseGet(() -> {
                        // If not in cache, get from repository
                        Product product = productRepository.findById(id)
                                .orElseThrow(() -> {
                                    log.warn("Product not found with ID: {}", id);
                                    return new ProductException.ProductNotFoundException(id);
                                });

                        ProductDTO productDTO = ProductDTO.fromDomain(product);

                        // Store in cache for future requests
                        productCacheService.cacheProduct(productDTO);

                        return productDTO;
                    });
        }
    }
} 