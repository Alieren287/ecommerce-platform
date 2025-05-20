package com.alier.ecommerceproductservice.infrastructure.controller;

import com.alier.ecommercecore.common.dto.BaseResponse;
import com.alier.ecommercecore.common.dto.PaginatedResponse;
import com.alier.ecommerceproductservice.application.dto.ProductVariantRequest;
import com.alier.ecommerceproductservice.application.dto.ProductVariantResponse;
import com.alier.ecommerceproductservice.application.usecase.variant.CreateProductVariantUseCase;
import com.alier.ecommerceproductservice.application.usecase.variant.DeleteProductVariantUseCase;
import com.alier.ecommerceproductservice.application.usecase.variant.GetProductVariantUseCase;
import com.alier.ecommerceproductservice.application.usecase.variant.UpdateProductVariantUseCase;
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

import java.util.List;
import java.util.UUID;

/**
 * REST controller for product variant operations.
 */
@RestController
@RequestMapping("/api/v1/products/{productId}/variants")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Product Variants", description = "Product variant management APIs")
public class ProductVariantController extends BaseController {

    private final CreateProductVariantUseCase createProductVariantUseCase;
    private final GetProductVariantUseCase getProductVariantUseCase;
    private final UpdateProductVariantUseCase updateProductVariantUseCase;
    private final DeleteProductVariantUseCase deleteProductVariantUseCase;

    @PostMapping
    @Operation(summary = "Create a new product variant", description = "Creates a new variant for a specific product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product variant created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Product variant with this SKU already exists")
    })
    public ResponseEntity<BaseResponse<ProductVariantResponse>> createProductVariant(
            @Parameter(description = "Product ID", required = true,
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("productId") UUID productId,
            
            @Valid @RequestBody ProductVariantRequest request) {
        log.info("REST request to create product variant for product ID: {}", productId);
        ProductVariantResponse createdVariant = createProductVariantUseCase.execute(productId, request);
        return created(createdVariant, "Product variant created successfully");
    }

    @GetMapping
    @Operation(summary = "Get all variants of a product", description = "Retrieves all variants for a specific product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product variants found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<List<ProductVariantResponse>>> getProductVariants(
            @Parameter(description = "Product ID", required = true,
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("productId") UUID productId) {
        log.info("REST request to get all variants for product ID: {}", productId);
        List<ProductVariantResponse> variants = getProductVariantUseCase.getByProductId(productId);
        return success(variants);
    }

    @GetMapping("/paged")
    @Operation(summary = "Get paginated variants of a product", 
            description = "Retrieves variants for a specific product with pagination and sorting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product variants found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductVariantResponse>>> getProductVariantsPaged(
            @Parameter(description = "Product ID", required = true,
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("productId") UUID productId,
            
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0", name = "page") @Min(0) int page,

            @Parameter(description = "Page size", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20", name = "size") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by", schema = @Schema(defaultValue = "id",
                    allowableValues = {"id", "name", "price", "stockQuantity", "sku", "createdAt", "updatedAt"}))
            @RequestParam(defaultValue = "id", name = "sortBy") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(defaultValue = "asc", 
                    allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc", name = "sortDir") String sortDir) {
        
        log.info("REST request to get paginated variants for product ID: {}, page: {}, size: {}", 
                productId, page, size);
        
        PaginatedResponse<ProductVariantResponse> variants = 
                getProductVariantUseCase.getByProductIdPaged(productId, page, size, sortBy, sortDir);
        
        return success(variants);
    }

    @GetMapping("/{variantId}")
    @Operation(summary = "Get a product variant by ID", description = "Retrieves a specific product variant by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product variant found"),
            @ApiResponse(responseCode = "404", description = "Product variant not found")
    })
    public ResponseEntity<BaseResponse<ProductVariantResponse>> getProductVariantById(
            @Parameter(description = "Variant ID", required = true,
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("variantId") UUID variantId) {
        
        log.info("REST request to get product variant by ID: {}", variantId);
        ProductVariantResponse variant = getProductVariantUseCase.getById(variantId);
        return success(variant);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get a product variant by SKU", description = "Retrieves a specific product variant by its SKU")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product variant found"),
            @ApiResponse(responseCode = "404", description = "Product variant not found with this SKU")
    })
    public ResponseEntity<BaseResponse<ProductVariantResponse>> getProductVariantBySku(
            @Parameter(description = "Variant SKU", required = true)
            @PathVariable("sku") String sku) {
        
        log.info("REST request to get product variant by SKU: {}", sku);
        ProductVariantResponse variant = getProductVariantUseCase.getBySku(sku);
        return success(variant);
    }

    @PutMapping("/{variantId}")
    @Operation(summary = "Update a product variant", description = "Updates an existing product variant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product variant updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product variant not found"),
            @ApiResponse(responseCode = "409", description = "Product variant with this SKU already exists")
    })
    public ResponseEntity<BaseResponse<ProductVariantResponse>> updateProductVariant(
            @Parameter(description = "Variant ID", required = true,
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("variantId") UUID variantId,
            
            @Valid @RequestBody ProductVariantRequest request) {
        
        log.info("REST request to update product variant with ID: {}", variantId);
        ProductVariantResponse updatedVariant = updateProductVariantUseCase.execute(variantId, request);
        return success(updatedVariant, "Product variant updated successfully");
    }

    @PatchMapping("/{variantId}/stock")
    @Operation(summary = "Update product variant stock", description = "Updates the stock quantity of a product variant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product variant stock updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stock quantity"),
            @ApiResponse(responseCode = "404", description = "Product variant not found")
    })
    public ResponseEntity<BaseResponse<ProductVariantResponse>> updateProductVariantStock(
            @Parameter(description = "Variant ID", required = true,
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("variantId") UUID variantId,
            
            @Parameter(description = "New stock quantity", required = true,
                    schema = @Schema(type = "integer", minimum = "0"))
            @RequestParam("quantity") @Min(0) Integer stockQuantity) {
        
        log.info("REST request to update stock for product variant with ID: {}, new quantity: {}", 
                variantId, stockQuantity);
        
        ProductVariantResponse updatedVariant = updateProductVariantUseCase.updateStock(variantId, stockQuantity);
        return success(updatedVariant, "Product variant stock updated successfully");
    }

    @DeleteMapping("/{variantId}")
    @Operation(summary = "Delete a product variant", description = "Deletes a product variant permanently")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product variant deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product variant not found")
    })
    public ResponseEntity<Void> deleteProductVariant(
            @Parameter(description = "Variant ID", required = true,
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("variantId") UUID variantId) {
        
        log.info("REST request to delete product variant with ID: {}", variantId);
        deleteProductVariantUseCase.execute(variantId);
        return noContent();
    }
} 