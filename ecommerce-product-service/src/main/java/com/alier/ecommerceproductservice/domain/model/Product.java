package com.alier.ecommerceproductservice.domain.model;

import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

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
                .imageUrls(new ArrayList<>())
                .status(ProductStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static void validateName(String name) {
        if (name == null) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_NAME_NULL);
        }

        if (name.trim().isEmpty()) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_NAME_EMPTY);
        }

        if (name.length() > 255) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_NAME_TOO_LONG);
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_PRICE_NULL);
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_PRICE_NEGATIVE);
        }

        if (price.compareTo(BigDecimal.ZERO) == 0) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_PRICE_ZERO);
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
     * Updates the product details with validation
     */
    public Product update(
            String name,
            String description,
            BigDecimal price,
            Integer stockQuantity) {

        validateName(name);
        validatePrice(price);
        validateStockQuantity(stockQuantity);

        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
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
            throw BusinessException.validation(ProductErrorCode.PRODUCT_ACTIVATION_NO_STOCK);
        }
        if (this.imageUrls == null || this.imageUrls.isEmpty()) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_ACTIVATION_NO_IMAGES);
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
            throw BusinessException.validation(ProductErrorCode.PRODUCT_DECREASE_QUANTITY_INVALID);
        }

        if (this.stockQuantity < quantity) {
            throw BusinessException.conflict(ProductErrorCode.PRODUCT_OUT_OF_STOCK);
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
            throw BusinessException.validation(ProductErrorCode.PRODUCT_INCREASE_QUANTITY_INVALID);
        }

        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    /**
     * Adds an image URL to the product.
     *
     * @param imageUrl The URL of the image to add.
     * @return The updated product.
     */
    public Product addImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_IMAGE_URL_EMPTY);
        }
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        if (!this.imageUrls.contains(imageUrl)) {
            this.imageUrls.add(imageUrl);
            this.updatedAt = LocalDateTime.now();
        }
        return this;
    }

    /**
     * Removes an image URL from the product.
     *
     * @param imageUrl The URL of the image to remove.
     * @return The updated product.
     */
    public Product removeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_IMAGE_URL_EMPTY);
        }
        if (this.imageUrls != null && this.imageUrls.remove(imageUrl)) {
            this.updatedAt = LocalDateTime.now();
        }
        return this;
    }

    /**
     * Clears all image URLs from the product.
     *
     * @return The updated product.
     */
    public Product clearImageUrls() {
        if (this.imageUrls != null && !this.imageUrls.isEmpty()) {
            this.imageUrls.clear();
            this.updatedAt = LocalDateTime.now();
        }
        return this;
    }
} 