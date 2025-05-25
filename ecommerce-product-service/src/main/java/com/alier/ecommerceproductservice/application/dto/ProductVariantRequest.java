package com.alier.ecommerceproductservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for creating a new product variant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new product variant")
public class ProductVariantRequest {

    @NotBlank(message = "Variant name is required")
    @Size(max = 255, message = "Variant name cannot exceed 255 characters")
    @Schema(description = "Name of the product variant", example = "Large Blue T-Shirt")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    @Schema(description = "Stock Keeping Unit (unique identifier)", example = "BLU-TSHIRT-L")
    private String sku;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    @Schema(description = "Price of the product variant", example = "29.99")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    @Schema(description = "Available quantity in stock", example = "100")
    private Integer stockQuantity;

    @Schema(description = "Additional attributes of the variant as key-value pairs",
            example = "{\"size\":\"L\",\"color\":\"blue\"}")
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();
} 