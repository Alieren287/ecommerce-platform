package com.alier.ecommerceproductservice.infrastructure.repository;

import com.alier.ecommerceproductservice.infrastructure.repository.entity.ProductVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for product variant entities.
 */
@Repository
public interface JpaProductVariantRepository extends JpaRepository<ProductVariantEntity, UUID> {

    /**
     * Find a product variant by its SKU
     *
     * @param sku the product variant SKU
     * @return an Optional containing the product variant if found, empty otherwise
     */
    Optional<ProductVariantEntity> findBySku(String sku);

    /**
     * Find all product variants for a product
     *
     * @param productId the product ID
     * @return list of product variants for the given product
     */
    List<ProductVariantEntity> findByProductId(UUID productId);

    /**
     * Find all product variants for a product with pagination
     *
     * @param productId the product ID
     * @param pageable  the pagination information
     * @return page of product variants for the given product
     */
    Page<ProductVariantEntity> findByProductId(UUID productId, Pageable pageable);

    /**
     * Check if a product variant with the given SKU exists
     *
     * @param sku the SKU to check
     * @return true if a product variant with the given SKU exists, false otherwise
     */
    boolean existsBySku(String sku);
} 