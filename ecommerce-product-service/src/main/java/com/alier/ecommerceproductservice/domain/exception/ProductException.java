package com.alier.ecommerceproductservice.domain.exception;

import com.alier.ecommercewebcore.rest.exception.RestBusinessException;

import java.util.UUID;

/**
 * Domain-specific exception for product-related errors.
 */
public class ProductException extends RestBusinessException {

    /**
     * Product-specific error with custom message
     *
     * @param productErrorCode The product-specific error code
     * @param message          Custom error message
     */
    public ProductException(ProductErrorCode productErrorCode, String message) {
        super(productErrorCode, message);
    }

    /**
     * Exception for when a product with the same SKU already exists
     */
    public static class ProductSkuAlreadyExistsException extends ProductException {
        public ProductSkuAlreadyExistsException(String sku) {
            super(ProductErrorCode.PRODUCT_SKU_EXISTS, "Product with SKU " + sku + " already exists");
        }
    }

    /**
     * Exception for when a product is out of stock
     */
    public static class ProductOutOfStockException extends ProductException {
        public ProductOutOfStockException(String productId) {
            super(ProductErrorCode.PRODUCT_OUT_OF_STOCK, "Product with ID " + productId + " is out of stock");
        }
    }

    /**
     * Exception for when a product cannot be found
     */
    public static class ProductNotFoundException extends ProductException {
        public ProductNotFoundException(String sku) {
            super(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with sku: " + sku);
        }

        public ProductNotFoundException(UUID id) {
            super(ProductErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id);
        }
    }
    
    /**
     * Exception for when a product variant with the same SKU already exists
     */
    public static class ProductVariantSkuAlreadyExistsException extends ProductException {
        public ProductVariantSkuAlreadyExistsException(String sku) {
            super(ProductErrorCode.PRODUCT_VARIANT_SKU_EXISTS, "Product variant with SKU " + sku + " already exists");
        }
    }
    
    /**
     * Exception for when a product variant is out of stock
     */
    public static class ProductVariantOutOfStockException extends ProductException {
        public ProductVariantOutOfStockException(String variantId) {
            super(ProductErrorCode.PRODUCT_VARIANT_OUT_OF_STOCK, "Product variant with ID " + variantId + " is out of stock");
        }
    }
    
    /**
     * Exception for when a product variant cannot be found
     */
    public static class ProductVariantNotFoundException extends ProductException {
        public ProductVariantNotFoundException(String sku) {
            super(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND, "Product variant not found with sku: " + sku);
        }
        
        public ProductVariantNotFoundException(UUID id) {
            super(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND, "Product variant not found with id: " + id);
        }
    }
} 