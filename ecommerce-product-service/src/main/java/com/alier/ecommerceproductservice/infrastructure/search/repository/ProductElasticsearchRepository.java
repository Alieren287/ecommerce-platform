package com.alier.ecommerceproductservice.infrastructure.search.repository;

import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import com.alier.ecommerceproductservice.infrastructure.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Elasticsearch repository for product search.
 */
@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * Full-text search across name and description
     */
    Page<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    /**
     * Search by SKU pattern
     */
    Page<ProductDocument> findBySkuContaining(String sku, Pageable pageable);

    /**
     * Search by status
     */
    Page<ProductDocument> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * Search by price range
     */
    Page<ProductDocument> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Search by stock range
     */
    Page<ProductDocument> findByStockQuantityBetween(Integer minStock, Integer maxStock, Pageable pageable);

    /**
     * Custom query for advanced search
     */
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"description\", \"sku^2\"], \"fuzziness\": \"AUTO\"}}]}}")
    Page<ProductDocument> search(String query, Pageable pageable);
} 