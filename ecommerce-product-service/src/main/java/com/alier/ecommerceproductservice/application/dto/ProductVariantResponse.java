package com.alier.ecommerceproductservice.application.dto;

import com.alier.ecommerceproductservice.domain.model.ProductVariant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for returning product variant information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing product variant details")
public class ProductVariantResponse {

    @Schema(description = "Unique identifier of the product variant", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Unique identifier of the parent product", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID productId;

    @Schema(description = "Name of the product variant", example = "Large Blue T-Shirt")
    private String name;

    @Schema(description = "Stock Keeping Unit (unique identifier)", example = "BLU-TSHIRT-L")
    private String sku;

    @Schema(description = "Price of the product variant", example = "29.99")
    private BigDecimal price;

    @Schema(description = "Available quantity in stock", example = "100")
    private Integer stockQuantity;

    @Schema(description = "Additional attributes of the variant as key-value pairs",
            example = "{\"size\":\"L\",\"color\":\"blue\"}")
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    @Schema(description = "Date and time when the product variant was created")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the product variant was last updated")
    private LocalDateTime updatedAt;

    /**
     * Factory method to create a ProductVariantResponse from a ProductVariant entity
     */
    public static ProductVariantResponse fromDomain(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProductId())
                .name(variant.getName())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .attributes(variant.getAttributes() != null
                        ? new HashMap<>(variant.getAttributes())
                        : new HashMap<>())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
} 