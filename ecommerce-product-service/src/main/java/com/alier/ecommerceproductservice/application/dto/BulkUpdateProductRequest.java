package com.alier.ecommerceproductservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request object for updating multiple products in a single operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for bulk update of products")
public class BulkUpdateProductRequest {

    @NotEmpty(message = "At least one product update is required")
    @Size(max = 100, message = "Maximum 100 products can be updated in a single request")
    @Valid
    @Schema(description = "List of product updates (max 100)")
    private List<ProductUpdate> updates;

    /**
     * Inner class representing a single product update.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Single product update information")
    public static class ProductUpdate {

        @NotNull(message = "Product ID is required")
        @Schema(description = "ID of the product to update", required = true)
        private UUID id;

        @NotNull(message = "Update data is required")
        @Valid
        @Schema(description = "New data for the product", required = true)
        private UpdateProductRequest data;
    }
} 