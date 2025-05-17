package com.alier.ecommerceproductservice.application.dto;

import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request object for patching a product (partial update).
 * All fields are optional, only the provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for partial update of a product")
public class ProductPatchRequest {

    @Size(max = 255, message = "Product name must be less than 255 characters")
    @Schema(description = "New product name")
    private String name;

    @Schema(description = "New product description")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Schema(description = "New product price")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(description = "New stock quantity")
    private Integer stockQuantity;

    @Schema(description = "New product status")
    private ProductStatus status;

    @Schema(description = "New product image URL")
    private String imageUrl;
} 