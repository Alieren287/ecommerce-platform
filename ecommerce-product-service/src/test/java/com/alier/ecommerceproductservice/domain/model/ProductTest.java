package com.alier.ecommerceproductservice.domain.model;

import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
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
        ProductException exception = assertThrows(ProductException.class, () -> {
            Product.create(name, description, price, stockQuantity, sku);
        });

        assertEquals(ProductErrorCode.INVALID_PRODUCT_NAME, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("cannot be empty"));
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
        ProductException exception = assertThrows(ProductException.class, () -> {
            Product.create(name, description, price, stockQuantity, sku);
        });

        assertEquals(ProductErrorCode.INVALID_PRODUCT_PRICE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("Should be able to activate product with stock")
    void shouldActivateProductWithStock() {
        // Given
        Product product = Product.create(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "TEST-SKU-123");

        // When
        product.activate();

        // Then
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
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
        ProductException exception = assertThrows(ProductException.class, () -> {
            product.activate();
        });

        assertEquals(ProductErrorCode.PRODUCT_ACTIVATION_FAILED, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Cannot activate"));
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
        ProductException.ProductOutOfStockException exception = assertThrows(ProductException.ProductOutOfStockException.class, () -> {
            product.decreaseStock(decreaseAmount);
        });

        assertEquals(ProductErrorCode.PRODUCT_OUT_OF_STOCK, exception.getErrorCode());
    }
} 