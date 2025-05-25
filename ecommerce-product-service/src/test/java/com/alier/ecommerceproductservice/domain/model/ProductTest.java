package com.alier.ecommerceproductservice.domain.model;

import com.alier.ecommercecore.common.exception.ValidationException;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Should create a valid product")
    void shouldCreateValidProduct() {
        // Given
        String name = "Test Product";
        String description = "Test Description";
        BigDecimal price = new BigDecimal("99.99");
        Integer stockQuantity = 10;
        String sku = "TEST-SKU-123";

        // When
        Product product = Product.create(name, description, price, stockQuantity, sku);

        // Then
        assertNotNull(product.getId());
        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(price, product.getPrice());
        assertEquals(stockQuantity, product.getStockQuantity());
        assertEquals(sku, product.getSku());
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    @DisplayName("Should throw exception when creating product with null name")
    void shouldThrowExceptionWhenNameIsNull() {
        // Given
        String name = null;
        String description = "Test Description";
        BigDecimal price = new BigDecimal("99.99");
        Integer stockQuantity = 10;
        String sku = "TEST-SKU-123";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            Product.create(name, description, price, stockQuantity, sku);
        });

        assertEquals(ProductErrorCode.PRODUCT_NAME_NULL, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("required"));
    }

    @Test
    @DisplayName("Should throw exception when creating product with negative price")
    void shouldThrowExceptionWhenPriceIsNegative() {
        // Given
        String name = "Test Product";
        String description = "Test Description";
        BigDecimal price = new BigDecimal("-10.00");
        Integer stockQuantity = 10;
        String sku = "TEST-SKU-123";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            Product.create(name, description, price, stockQuantity, sku);
        });

        assertEquals(ProductErrorCode.PRODUCT_PRICE_NEGATIVE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("Should be able to activate product with stock")
    void shouldNotActivateProductWithoutImage() {
        // Given
        Product product = Product.create(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "TEST-SKU-123");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            product.activate();
        });

        assertEquals(ProductErrorCode.PRODUCT_ACTIVATION_NO_IMAGES, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("no images"));
    }

    @Test
    @DisplayName("Should not be able to activate product without stock")
    void shouldNotActivateProductWithoutStock() {
        // Given
        Product product = Product.create(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                0,
                "TEST-SKU-123");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            product.activate();
        });

        assertEquals(ProductErrorCode.PRODUCT_ACTIVATION_NO_STOCK, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("no stock"));
    }

    @Test
    @DisplayName("Should decrease stock successfully")
    void shouldDecreaseStockSuccessfully() {
        // Given
        Product product = Product.create(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "TEST-SKU-123");
        int decreaseAmount = 3;
        int expectedStock = 7;

        // When
        product.decreaseStock(decreaseAmount);

        // Then
        assertEquals(expectedStock, product.getStockQuantity());
    }

    @Test
    @DisplayName("Should throw exception when decreasing more than available stock")
    void shouldThrowExceptionWhenDecreasingMoreThanAvailableStock() {
        // Given
        Product product = Product.create(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "TEST-SKU-123");
        int decreaseAmount = 15;

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            product.decreaseStock(decreaseAmount);
        });

        assertEquals(ProductErrorCode.STOCK_QUANTITY_IS_NOT_ENOUGH, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("is not enough"));
    }
} 