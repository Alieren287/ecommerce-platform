package com.alier.ecommerceproductservice.domain.event;

import com.alier.ecommercecore.common.event.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event emitted when a new product is created.
 */
@Getter
@Builder
public class ProductCreatedEvent implements DomainEvent {

    private final UUID id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final String sku;
    @Builder.Default
    private final LocalDateTime occurredOn = LocalDateTime.now();

    /**
     * Factory method to create a ProductCreatedEvent from a Product entity
     */
    public static ProductCreatedEvent fromProduct(com.alier.ecommerceproductservice.domain.model.Product product) {
        return ProductCreatedEvent.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .build();
    }

    @Override
    public String getType() {
        return "PRODUCT_CREATED";
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
} 