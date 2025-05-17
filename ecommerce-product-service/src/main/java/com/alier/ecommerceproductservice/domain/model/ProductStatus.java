package com.alier.ecommerceproductservice.domain.model;

/**
 * Represents the possible states of a product in the system.
 */
public enum ProductStatus {
    /**
     * Product is in creation/editing phase, not yet visible to customers
     */
    DRAFT,

    /**
     * Product is visible and available for purchase
     */
    ACTIVE,

    /**
     * Product exists but is not available for purchase
     */
    INACTIVE,

    /**
     * Product has been discontinued
     */
    DISCONTINUED
} 