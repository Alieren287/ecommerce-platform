package com.alier.ecommerceproductservice.domain.event;

import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event representing a change in product status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusChangedEvent {

    /**
     * The product ID
     */
    private UUID id;

    /**
     * The product SKU
     */
    private String sku;

    /**
     * The product's new status
     */
    private ProductStatus status;

    /**
     * The type of status change (e.g., "ACTIVATED", "DEACTIVATED")
     */
    private String statusChangeType;

    /**
     * The timestamp when the status change occurred
     */
    private LocalDateTime timestamp;
} 