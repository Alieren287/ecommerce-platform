package com.alier.ecommerceproductservice.domain.model;

import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ProductVariant domain model representing different variants of a product
 * like different sizes, colors, etc.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductVariant {

    private UUID id;

    private UUID productId;

    private String name;

    private String sku;

    private BigDecimal price;

    private Integer stockQuantity;

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Creates a new product variant with validation
     */
    public static ProductVariant create(
            UUID productId,
            String name,
            String sku,
            BigDecimal price,
            Integer stockQuantity,
            Map<String, Object> attributes) {

        validateName(name);
        validatePrice(price);
        validateStockQuantity(stockQuantity);

        LocalDateTime now = LocalDateTime.now();

        return ProductVariant.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .name(name)
                .sku(sku)
                .price(price)
                .stockQuantity(stockQuantity)
                .attributes(attributes != null ? attributes : new HashMap<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static void validateName(String name) {
        if (name == null) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_NAME_NULL);
        }

        if (name.trim().isEmpty()) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_NAME_EMPTY);
        }

        if (name.length() > 255) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_NAME_TOO_LONG);
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_PRICE_NULL);
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_PRICE_NEGATIVE);
        }

        if (price.compareTo(BigDecimal.ZERO) == 0) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_PRICE_ZERO);
        }
    }

    private static void validateStockQuantity(Integer stockQuantity) {
        if (stockQuantity == null) {
            throw BusinessException.validation(ProductErrorCode.STOCK_QUANTITY_NULL);
        }

        if (stockQuantity < 0) {
            throw BusinessException.validation(ProductErrorCode.STOCK_QUANTITY_NEGATIVE);
        }
    }

    /**
     * Updates the product variant details with validation
     */
    public ProductVariant update(
            String name,
            BigDecimal price,
            Integer stockQuantity,
            Map<String, Object> attributes) {

        validateName(name);
        validatePrice(price);
        validateStockQuantity(stockQuantity);

        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        }
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    /**
     * Updates stock quantity with validation
     */
    public ProductVariant updateStock(Integer newStockQuantity) {
        validateStockQuantity(newStockQuantity);
        this.stockQuantity = newStockQuantity;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * Decreases the stock quantity by the given amount
     */
    public ProductVariant decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_DECREASE_QUANTITY_INVALID);
        }

        if (this.stockQuantity < quantity) {
            throw BusinessException.conflict(ProductErrorCode.PRODUCT_VARIANT_OUT_OF_STOCK);
        }

        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    /**
     * Increases the stock quantity by the given amount
     */
    public ProductVariant increaseStock(int quantity) {
        if (quantity <= 0) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_INCREASE_QUANTITY_INVALID);
        }

        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    /**
     * Adds or updates an attribute
     */
    public ProductVariant setAttribute(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_ATTRIBUTE_KEY_EMPTY);
        }
        this.attributes.put(key, value);
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * Removes an attribute
     */
    public ProductVariant removeAttribute(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_VARIANT_ATTRIBUTE_KEY_EMPTY);
        }
        this.attributes.remove(key);
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * Gets an attribute value
     */
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }
} 