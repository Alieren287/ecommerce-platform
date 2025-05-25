package com.alier.ecommerceproductservice.infrastructure.controller;

import com.alier.ecommercecore.common.dto.BaseResponse;
import com.alier.ecommercecore.common.dto.PaginatedResponse;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommerceproductservice.application.dto.*;
import com.alier.ecommerceproductservice.application.usecase.CreateProductUseCase;
import com.alier.ecommerceproductservice.application.usecase.GetProductUseCase;
import com.alier.ecommerceproductservice.application.usecase.UpdateProductUseCase;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.infrastructure.search.ProductSearchService;
import com.alier.ecommercewebcore.rest.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for product operations.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Products", description = "Product management APIs (excluding images)")
public class ProductController extends BaseController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ProductSearchService searchService;
    private final ProductRepository productRepository;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product with the given details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Product with this SKU already exists")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.info("REST request to create product: {}", request);
        ProductDTO createdProduct = createProductUseCase.execute(request);
        return created(createdProduct, "Product created successfully");
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple products", description = "Creates multiple products in a single operation (max 100)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Products created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "One or more products with these SKUs already exist")
    })
    public ResponseEntity<BaseResponse<List<ProductDTO>>> createProductsBulk(
            @Valid @RequestBody BulkCreateProductRequest request) {
        log.info("REST request to bulk create {} products", request.getProducts().size());
        List<ProductDTO> createdProducts = createProductUseCase.executeBulk(request);
        return created(createdProducts, "Products created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> getProductById(
            @Parameter(description = "Product ID", required = true,
                    schema = @Schema(type = "string", format = "uuid",
                            example = "123e4567-e89b-12d3-a456-426614174000"))
            @PathVariable("id") UUID id) {
        log.info("REST request to get product by ID: {}", id);
        ProductDTO product = getProductUseCase.getById(id);
        return success(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves a product by its SKU")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found with this SKU")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> getProductBySku(
            @Parameter(description = "Product SKU", required = true)
            @PathVariable("sku") String sku) {
        log.info("REST request to get product by SKU: {}", sku);
        ProductDTO product = getProductUseCase.getBySku(sku);
        return success(product);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination and sorting",
            description = "Retrieves a paginated list of products with optional sorting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDTO>>> getAllProducts(
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0", name = "page") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20", name = "size") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by", schema = @Schema(defaultValue = "id",
                    allowableValues = {"id", "name", "price", "stockQuantity", "sku", "status", "createdAt", "updatedAt"}))
            @RequestParam(defaultValue = "id", name = "sortBy") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(defaultValue = "asc", allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc", name = "sortDir") String sortDir) {

        log.info("REST request to get paginated products (page={}, size={})", page, size);
        PaginatedResponse<ProductDTO> products = getProductUseCase.getAllProductsPaged(page, size, sortBy, sortDir);
        return success(products);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter products with flexible criteria",
            description = "Searches and filters products with multiple optional criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDTO>>> filterProducts(
            @Parameter(description = "Filter by product name (partial match, case-insensitive)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by SKU (partial match, case-insensitive)")
            @RequestParam(required = false) String sku,

            @Parameter(description = "Filter by product status")
            @RequestParam(required = false) ProductStatus status,

            @Parameter(description = "Filter by minimum price")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Filter by maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Filter by minimum stock quantity")
            @RequestParam(required = false) Integer minStock,

            @Parameter(description = "Filter by maximum stock quantity")
            @RequestParam(required = false) Integer maxStock,

            @Parameter(description = "General search term for name, description or SKU")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by", schema = @Schema(defaultValue = "id"))
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(defaultValue = "asc", allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("REST request to filter products with criteria");

        // Build filter request from query parameters
        ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                .name(name)
                .sku(sku)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minStock(minStock)
                .maxStock(maxStock)
                .searchTerm(searchTerm)
                .build();

        PaginatedResponse<ProductDTO> products = getProductUseCase.searchProducts(filterRequest, page, size, sortBy, sortDir);
        return success(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches for products by name, description or SKU")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDTO>>> searchProducts(
            @Parameter(description = "Search query", required = true)
            @RequestParam String query,

            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by", schema = @Schema(defaultValue = "id"))
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(defaultValue = "asc", allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("REST request to search products with query: {}", query);
        PaginatedResponse<ProductDTO> products = getProductUseCase.simpleSearch(query, page, size, sortBy, sortDir);
        return success(products);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "Updates an existing product with new details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> updateProduct(
            @Parameter(description = "Product ID", required = true,
                    schema = @Schema(type = "string", format = "uuid",
                            example = "123e4567-e89b-12d3-a456-426614174000"))
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("REST request to update product with ID: {}", id);
        ProductDTO updatedProduct = updateProductUseCase.execute(id, request);
        return success(updatedProduct, "Product updated successfully");
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a product",
            description = "Updates specific fields of an existing product (only the provided fields will be updated)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product patched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> patchProduct(
            @Parameter(description = "Product ID", required = true,
                    schema = @Schema(type = "string", format = "uuid",
                            example = "123e4567-e89b-12d3-a456-426614174000"))
            @PathVariable("id") UUID id,
            @Valid @RequestBody ProductPatchRequest request) {
        log.info("REST request to patch product with ID: {}", id);
        ProductDTO patchedProduct = updateProductUseCase.executePatch(id, request);
        return success(patchedProduct, "Product patched successfully");
    }

    @PutMapping("/bulk")
    @Operation(summary = "Update multiple products", description = "Updates multiple products in a single operation (max 100)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "One or more products not found")
    })
    public ResponseEntity<BaseResponse<List<ProductDTO>>> updateProductsBulk(
            @Valid @RequestBody BulkUpdateProductRequest request) {
        log.info("REST request to bulk update {} products", request.getUpdates().size());
        List<ProductDTO> updatedProducts = updateProductUseCase.executeBulk(request);
        return success(updatedProducts, "Products updated successfully");
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate a product", description = "Changes a product status to ACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product activated successfully"),
            @ApiResponse(responseCode = "400", description = "Product cannot be activated (e.g., out of stock)"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> activateProduct(
            @Parameter(description = "Product ID", required = true,
                    schema = @Schema(type = "string", format = "uuid",
                            example = "123e4567-e89b-12d3-a456-426614174000"))
            @PathVariable("id") UUID id) {
        log.info("REST request to activate product with ID: {}", id);
        ProductDTO activatedProduct = updateProductUseCase.activateProduct(id);
        return success(activatedProduct, "Product activated successfully");
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a product", description = "Changes a product status to INACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<ProductDTO>> deactivateProduct(
            @Parameter(description = "Product ID", required = true,
                    schema = @Schema(type = "string", format = "uuid",
                            example = "123e4567-e89b-12d3-a456-426614174000"))
            @PathVariable("id") UUID id) {
        log.info("REST request to deactivate product with ID: {}", id);
        ProductDTO deactivatedProduct = updateProductUseCase.deactivateProduct(id);
        return success(deactivatedProduct, "Product deactivated successfully");
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get products by status",
            description = "Retrieves a paginated list of products with a specific status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDTO>>> getProductsByStatus(
            @Parameter(description = "Product status", required = true)
            @PathVariable("status") ProductStatus status,

            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by", schema = @Schema(defaultValue = "id"))
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(defaultValue = "asc", allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("REST request to get products with status: {}", status);
        PaginatedResponse<ProductDTO> products = getProductUseCase.getProductsByStatus(status, page, size, sortBy, sortDir);
        return success(products);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range",
            description = "Retrieves a paginated list of products within a specific price range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found"),
            @ApiResponse(responseCode = "400", description = "Invalid price range")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDTO>>> getProductsByPriceRange(
            @Parameter(description = "Minimum price", required = true)
            @RequestParam BigDecimal minPrice,

            @Parameter(description = "Maximum price", required = true)
            @RequestParam BigDecimal maxPrice,

            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by", schema = @Schema(defaultValue = "id"))
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(defaultValue = "asc", allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("REST request to get products with price between {} and {}", minPrice, maxPrice);

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.validation(ProductErrorCode.INVALID_PRICE_RANGE, "Minimum price cannot be negative");
        }

        if (minPrice != null && maxPrice != null && maxPrice.compareTo(minPrice) < 0) {
            throw BusinessException.validation(ProductErrorCode.INVALID_PRICE_RANGE, "Maximum price cannot be less than minimum price");
        }

        PaginatedResponse<ProductDTO> products = getProductUseCase.getProductsByPriceRange(minPrice, maxPrice, page, size, sortBy, sortDir);
        return success(products);
    }

    @GetMapping("/elasticsearch/search")
    @Operation(summary = "Advanced search with Elasticsearch",
            description = "Performs full-text search across product fields with relevance ranking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDTO>>> elasticsearchSearch(
            @Parameter(description = "Search query", required = true)
            @RequestParam String query,

            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by", schema = @Schema(defaultValue = "id"))
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(defaultValue = "asc", allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("REST request to search products with Elasticsearch query: {}", query);
        PaginatedResponse<ProductDTO> products = searchService.searchProducts(query, page, size, sortBy, sortDir);
        return success(products);
    }

    @GetMapping("/elasticsearch/filter")
    @Operation(summary = "Advanced filter with Elasticsearch",
            description = "Filters products using Elasticsearch for better performance with large datasets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered products")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDTO>>> elasticsearchFilter(
            @Parameter(description = "Filter by product name (partial match, case-insensitive)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by SKU (partial match, case-insensitive)")
            @RequestParam(required = false) String sku,

            @Parameter(description = "Filter by product status")
            @RequestParam(required = false) ProductStatus status,

            @Parameter(description = "Filter by minimum price")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Filter by maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Filter by minimum stock quantity")
            @RequestParam(required = false) Integer minStock,

            @Parameter(description = "Filter by maximum stock quantity")
            @RequestParam(required = false) Integer maxStock,

            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by", schema = @Schema(defaultValue = "id"))
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(defaultValue = "asc", allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("REST request to filter products using Elasticsearch");

        // Build filter request from query parameters
        ProductFilterRequest filterRequest = ProductFilterRequest.builder()
                .name(name)
                .sku(sku)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minStock(minStock)
                .maxStock(maxStock)
                .build();

        PaginatedResponse<ProductDTO> products = searchService.filterProducts(filterRequest, page, size, sortBy, sortDir);
        return success(products);
    }

    @PostMapping("/elasticsearch/raw-query")
    @Operation(summary = "Execute raw Elasticsearch query",
            description = "Advanced users can send custom Elasticsearch queries in JSON format")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Query results"),
            @ApiResponse(responseCode = "400", description = "Invalid query")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDTO>>> executeRawQuery(
            @Parameter(description = "Raw Elasticsearch query (JSON format)", required = true)
            @RequestBody String queryJson,

            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("REST request to execute raw Elasticsearch query");
        PaginatedResponse<ProductDTO> products = searchService.executeRawQuery(queryJson, page, size);
        return success(products);
    }

    @PostMapping("/elasticsearch/reindex")
    @Operation(summary = "Reindex all products",
            description = "Rebuilds the Elasticsearch index with current data from the primary database (admin only). This can be a long-running operation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reindexing initiated successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role - to be implemented)"),
            @ApiResponse(responseCode = "500", description = "Reindexing failed")
    })
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") or similar security constraint
    public ResponseEntity<BaseResponse<Void>> reindexProducts() {
        log.info("REST request to reindex all products in Elasticsearch");
        try {
            long count = productRepository.count(); // This should work as JpaRepository provides count()
            if (count == 0) {
                log.info("No products found in the database to reindex.");
                return success(null, "No products to reindex.");
            }

            log.info("Fetching {} products from database for reindexing...", count);
            List<Product> allProducts = productRepository.findAll(); // Fetch all products

            if (allProducts.isEmpty() && count > 0) {
                // This case might indicate an issue or a race condition, but proceed with count for logging
                log.warn("Product count is {} but findAll() returned an empty list. Proceeding with reindex based on count.", count);
                // If genuinely no products despite count, treat as no products to reindex to avoid error with empty list to reindexAll
                if (allProducts.isEmpty()) { // Re-check after findAll
                    return success(null, "No products to reindex after attempting to fetch all.");
                }
            } else if (allProducts.isEmpty()) {
                return success(null, "No products to reindex.");
            }

            log.info("Starting reindexing of {} products in Elasticsearch.", allProducts.size());
            searchService.reindexAll(allProducts); // Pass the fetched products
            log.info("Successfully initiated reindexing of {} products.", allProducts.size());
            return success(null, "Product reindexing initiated for " + allProducts.size() + " products.");

        } catch (Exception e) {
            log.error("Error during product reindexing: {}", e.getMessage(), e);
            throw BusinessException.internalServer(ProductErrorCode.PRODUCT_REINDEX_FAILED, "Failed to reindex products: " + e.getMessage());
        }
    }

    // --- Product Image Management Endpoints have been moved to ProductImageController ---

} 