package com.alier.ecommerceproductservice.domain.repository;

import com.alier.ecommerceproductservice.domain.model.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ProductVariant domain object.
 * This is a domain interface (port) that will be implemented by an adapter
 * in the infrastructure layer.
 */
public interface ProductVariantRepository {

    /**
     * Save a product variant
     *
     * @param productVariant the product variant to save
     * @return the saved product variant
     */
    ProductVariant save(ProductVariant productVariant);

    /**
     * Save multiple product variants in a batch operation
     *
     * @param productVariants the list of product variants to save
     * @return the list of saved product variants
     */
    List<ProductVariant> saveAll(List<ProductVariant> productVariants);

    /**
     * Find a product variant by its ID
     *
     * @param id the product variant ID
     * @return an Optional containing the product variant if found, empty otherwise
     */
    Optional<ProductVariant> findById(UUID id);

    /**
     * Find a product variant by its SKU
     *
     * @param sku the product variant SKU
     * @return an Optional containing the product variant if found, empty otherwise
     */
    Optional<ProductVariant> findBySku(String sku);

    /**
     * Find all product variants for a product
     *
     * @param productId the product ID
     * @return list of product variants for the given product
     */
    List<ProductVariant> findByProductId(UUID productId);

    /**
     * Find all product variants for a product with pagination
     *
     * @param productId the product ID
     * @param pageable  the pagination information
     * @return page of product variants for the given product
     */
    Page<ProductVariant> findByProductId(UUID productId, Pageable pageable);

    /**
     * Delete a product variant by ID
     *
     * @param id the product variant ID
     */
    void deleteById(UUID id);

    /**
     * Check if a product variant with the given SKU exists
     *
     * @param sku the SKU to check
     * @return true if a product variant with the given SKU exists, false otherwise
     */
    boolean existsBySku(String sku);
} 