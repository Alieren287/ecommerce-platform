package com.alier.ecommerceproductservice.domain.model;

import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product aggregate root for the product domain.
 * Contains core business logic and validation rules.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

    private UUID id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stockQuantity;

    private String sku;

    private String imageUrl;

    private ProductStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Creates a new product with basic validation
     */
    public static Product create(
            String name,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            String sku) {

        validateName(name);
        validatePrice(price);
        validateStockQuantity(stockQuantity);

        LocalDateTime now = LocalDateTime.now();

        return Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .sku(sku)
                .status(ProductStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_NAME,
                    "Product name cannot be empty");
        }

        if (name.length() > 255) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_NAME,
                    "Product name is too long (max 255 characters)");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_PRICE,
                    "Price cannot be null");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_PRICE,
                    "Price cannot be negative");
        }
    }

    private static void validateStockQuantity(Integer stockQuantity) {
        if (stockQuantity == null) {
            throw new ProductException(ProductErrorCode.INVALID_STOCK_QUANTITY,
                    "Stock quantity cannot be null");
        }

        if (stockQuantity < 0) {
            throw new ProductException(ProductErrorCode.INVALID_STOCK_QUANTITY,
                    "Stock quantity cannot be negative");
        }
    }

    /**
     * Updates the product details with validation
     */
    public Product update(
            String name,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            String imageUrl) {

        validateName(name);
        validatePrice(price);
        validateStockQuantity(stockQuantity);

        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    /**
     * Updates stock quantity with validation
     */
    public Product updateStock(Integer newStockQuantity) {
        validateStockQuantity(newStockQuantity);
        this.stockQuantity = newStockQuantity;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * Activates a product making it available for purchase
     */
    public Product activate() {
        if (this.stockQuantity <= 0) {
            throw new ProductException(ProductErrorCode.PRODUCT_ACTIVATION_FAILED,
                    "Cannot activate a product with no stock");
        }

        this.status = ProductStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    // Validation methods

    /**
     * Deactivates a product making it unavailable for purchase
     */
    public Product deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * Decreases the stock quantity by the given amount
     */
    public Product decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_OPERATION,
                    "Quantity to decrease must be positive");
        }

        if (this.stockQuantity < quantity) {
            throw new ProductException.ProductOutOfStockException(this.id.toString());
        }

        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    /**
     * Increases the stock quantity by the given amount
     */
    public Product increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_OPERATION,
                    "Quantity to increase must be positive");
        }

        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();

        return this;
    }
} 