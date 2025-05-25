package com.alier.ecommerceproductservice.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductErrorMessagesTest {

    @Test
    void testProductNameValidationMessages() {
        // Test specific error messages for different name validation failures
        assertEquals("Product name is required and cannot be null",
                ProductErrorMessages.PRODUCT_NAME_CANNOT_BE_NULL);

        assertEquals("Product name is required and cannot be empty or contain only whitespace",
                ProductErrorMessages.PRODUCT_NAME_CANNOT_BE_EMPTY);

        assertEquals("Product name exceeds the maximum allowed length of 255 characters",
                ProductErrorMessages.PRODUCT_NAME_TOO_LONG);
    }

    @Test
    void testProductPriceValidationMessages() {
        // Test specific error messages for different price validation failures
        assertEquals("Product price is required and cannot be null",
                ProductErrorMessages.PRODUCT_PRICE_CANNOT_BE_NULL);

        assertEquals("Product price cannot be negative. Please provide a valid positive price",
                ProductErrorMessages.PRODUCT_PRICE_CANNOT_BE_NEGATIVE);

        assertEquals("Product price cannot be zero. Please provide a valid positive price",
                ProductErrorMessages.PRODUCT_PRICE_CANNOT_BE_ZERO);
    }

    @Test
    void testStockQuantityValidationMessages() {
        // Test specific error messages for stock quantity validation failures
        assertEquals("Stock quantity is required and cannot be null",
                ProductErrorMessages.STOCK_QUANTITY_CANNOT_BE_NULL);

        assertEquals("Stock quantity cannot be negative. Please provide a valid quantity of zero or more",
                ProductErrorMessages.STOCK_QUANTITY_CANNOT_BE_NEGATIVE);
    }

    @Test
    void testProductVariantValidationMessages() {
        // Test specific error messages for product variant validation failures
        assertEquals("Product variant name is required and cannot be null",
                ProductErrorMessages.PRODUCT_VARIANT_NAME_CANNOT_BE_NULL);

        assertEquals("Product variant price cannot be negative. Please provide a valid positive price",
                ProductErrorMessages.PRODUCT_VARIANT_PRICE_CANNOT_BE_NEGATIVE);

        assertEquals("Product variant price cannot be zero. Please provide a valid positive price",
                ProductErrorMessages.PRODUCT_VARIANT_PRICE_CANNOT_BE_ZERO);
    }

    @Test
    void testOperationMessages() {
        // Test specific error messages for operation failures
        assertEquals("Cannot activate product because it has no stock. Please add inventory before activation",
                ProductErrorMessages.PRODUCT_ACTIVATION_REQUIRES_STOCK);

        assertEquals("Cannot activate product because it has no images. Please upload at least one product image before activation",
                ProductErrorMessages.PRODUCT_ACTIVATION_REQUIRES_IMAGES);

        assertEquals("Quantity to decrease must be a positive number greater than zero",
                ProductErrorMessages.PRODUCT_DECREASE_QUANTITY_MUST_BE_POSITIVE);

        assertEquals("Image URL cannot be empty or contain only whitespace",
                ProductErrorMessages.PRODUCT_IMAGE_URL_CANNOT_BE_EMPTY);
    }

    @Test
    void testConflictMessages() {
        // Test specific error messages for conflict scenarios
        assertEquals("A product with this SKU already exists in the system",
                ProductErrorMessages.PRODUCT_SKU_ALREADY_EXISTS);

        assertEquals("Product is currently out of stock and cannot be purchased",
                ProductErrorMessages.PRODUCT_OUT_OF_STOCK);

        assertEquals("A product variant with this SKU already exists in the system",
                ProductErrorMessages.PRODUCT_VARIANT_SKU_ALREADY_EXISTS);
    }

    @Test
    void testNotFoundMessages() {
        // Test specific error messages for not found scenarios
        assertEquals("Product not found with the specified SKU",
                ProductErrorMessages.PRODUCT_NOT_FOUND_BY_SKU);

        assertEquals("Product not found with the specified ID",
                ProductErrorMessages.PRODUCT_NOT_FOUND_BY_ID);

        assertEquals("Product variant not found with the specified SKU",
                ProductErrorMessages.PRODUCT_VARIANT_NOT_FOUND_BY_SKU);

        assertEquals("Product variant not found with the specified ID",
                ProductErrorMessages.PRODUCT_VARIANT_NOT_FOUND_BY_ID);
    }

    @Test
    void testBusinessOperationMessages() {
        // Test specific error messages for business operations
        assertEquals("Discount percentage must be between 0 and 100",
                ProductErrorMessages.PRODUCT_DISCOUNT_PERCENTAGE_INVALID);

        assertEquals("Attribute key cannot be empty or contain only whitespace",
                ProductErrorMessages.PRODUCT_VARIANT_ATTRIBUTE_KEY_CANNOT_BE_EMPTY);

        assertEquals("Product update operation failed due to a system error. Please try again",
                ProductErrorMessages.PRODUCT_UPDATE_OPERATION_FAILED);
    }

    @Test
    void testAllMessagesAreInformativeAndSpecific() {
        // Verify that all messages provide clear, specific information about the failure

        // Each message should be clear about what went wrong
        assertTrue(ProductErrorMessages.PRODUCT_NAME_CANNOT_BE_NULL.contains("required"));
        assertTrue(ProductErrorMessages.PRODUCT_PRICE_CANNOT_BE_NEGATIVE.contains("cannot be negative"));
        assertTrue(ProductErrorMessages.PRODUCT_ACTIVATION_REQUIRES_STOCK.contains("no stock"));

        // Each message should provide guidance when possible
        assertTrue(ProductErrorMessages.PRODUCT_PRICE_CANNOT_BE_NEGATIVE.contains("Please provide"));
        assertTrue(ProductErrorMessages.PRODUCT_ACTIVATION_REQUIRES_IMAGES.contains("Please upload"));
        assertTrue(ProductErrorMessages.STOCK_QUANTITY_CANNOT_BE_NEGATIVE.contains("Please provide"));

        // Messages should be specific about the context
        assertTrue(ProductErrorMessages.PRODUCT_VARIANT_NAME_CANNOT_BE_NULL.contains("variant"));
        assertTrue(ProductErrorMessages.PRODUCT_ACTIVATION_REQUIRES_IMAGES.contains("product image"));
        assertTrue(ProductErrorMessages.PRODUCT_DISCOUNT_PERCENTAGE_INVALID.contains("0 and 100"));
    }
} 