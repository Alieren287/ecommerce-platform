package com.alier.ecommerceproductservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request object for creating multiple products in a single operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for bulk creation of products")
public class BulkCreateProductRequest {

    @NotEmpty(message = "At least one product is required")
    @Size(max = 100, message = "Maximum 100 products can be created in a single request")
    @Valid
    @Schema(description = "List of products to create (max 100)")
    private List<CreateProductRequest> products;
} 