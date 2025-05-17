package com.alier.ecommerceproductservice.infrastructure.repository;

import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import com.alier.ecommerceproductservice.infrastructure.repository.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for product entities.
 * Extended with pagination and filtering capabilities.
 */
@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

    /**
     * Find a product entity by SKU
     */
    Optional<ProductEntity> findBySku(String sku);

    /**
     * Find all product entities with a specific status
     */
    List<ProductEntity> findByStatus(ProductStatus status);

    /**
     * Find all product entities with a specific status with pagination
     */
    Page<ProductEntity> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * Find products by name (partial match, case-insensitive)
     */
    Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find products by description (partial match, case-insensitive)
     */
    Page<ProductEntity> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * Find products by price range
     */
    Page<ProductEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find products by stock quantity range
     */
    Page<ProductEntity> findByStockQuantityBetween(Integer minStock, Integer maxStock, Pageable pageable);

    /**
     * Find products by SKU pattern (partial match, case-insensitive)
     */
    Page<ProductEntity> findBySkuContainingIgnoreCase(String sku, Pageable pageable);

    /**
     * Advanced search query with multiple criteria (name, description or SKU)
     */
    @Query("SELECT p FROM ProductEntity p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ProductEntity> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products by combination of filters
     */
    @Query("SELECT p FROM ProductEntity p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:minStock IS NULL OR p.stockQuantity >= :minStock) AND " +
            "(:maxStock IS NULL OR p.stockQuantity <= :maxStock)")
    Page<ProductEntity> findByFilters(
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("status") ProductStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock,
            Pageable pageable);

    /**
     * Check if a product with the given SKU exists
     */
    boolean existsBySku(String sku);
} 