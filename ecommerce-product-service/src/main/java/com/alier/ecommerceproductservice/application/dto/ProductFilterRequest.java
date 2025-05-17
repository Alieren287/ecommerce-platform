package com.alier.ecommerceproductservice.application.dto;

import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request object for filtering products with flexible search parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter criteria for product search")
public class ProductFilterRequest {

    @Schema(description = "Filter by product name (partial match, case-insensitive)")
    private String name;

    @Schema(description = "Filter by SKU (partial match, case-insensitive)")
    private String sku;

    @Schema(description = "Filter by product status")
    private ProductStatus status;

    @Schema(description = "Filter by minimum price")
    private BigDecimal minPrice;

    @Schema(description = "Filter by maximum price")
    private BigDecimal maxPrice;

    @Schema(description = "Filter by minimum stock quantity")
    private Integer minStock;

    @Schema(description = "Filter by maximum stock quantity")
    private Integer maxStock;

    @Schema(description = "General search term for name, description or SKU")
    private String searchTerm;
} 