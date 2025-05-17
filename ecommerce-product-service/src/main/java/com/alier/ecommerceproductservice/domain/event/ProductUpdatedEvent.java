package com.alier.ecommerceproductservice.domain.event;

import com.alier.ecommercecore.common.event.DomainEvent;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event emitted when a product is updated.
 */
@Getter
@Builder
public class ProductUpdatedEvent implements DomainEvent {

    private final UUID id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final ProductStatus status;
    @Builder.Default
    private final LocalDateTime occurredOn = LocalDateTime.now();

    /**
     * Factory method to create a ProductUpdatedEvent from a Product entity
     */
    public static ProductUpdatedEvent fromProduct(com.alier.ecommerceproductservice.domain.model.Product product) {
        return ProductUpdatedEvent.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .build();
    }

    @Override
    public String getType() {
        return "PRODUCT_UPDATED";
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 