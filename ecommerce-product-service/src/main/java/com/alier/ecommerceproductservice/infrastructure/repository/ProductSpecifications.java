package com.alier.ecommerceproductservice.infrastructure.repository;

import com.alier.ecommerceproductservice.application.dto.ProductFilterRequest;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import com.alier.ecommerceproductservice.infrastructure.repository.entity.ProductEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Utility class for creating Spring Data JPA Specifications for ProductEntity.
 * These specifications enable complex dynamic filtering of products.
 */
public class ProductSpecifications {

    /**
     * Create a specification based on a filter request.
     *
     * @param filter The filter request containing search criteria
     * @return A specification that combines all the filter criteria
     */
    public static Specification<ProductEntity> withFilters(ProductFilterRequest filter) {
        return Specification
                .where(nameContains(filter.getName()))
                .and(skuContains(filter.getSku()))
                .and(hasStatus(filter.getStatus()))
                .and(priceBetween(filter.getMinPrice(), filter.getMaxPrice()))
                .and(stockBetween(filter.getMinStock(), filter.getMaxStock()))
                .and(generalSearch(filter.getSearchTerm()));
    }

    /**
     * Filter by name (case-insensitive partial match)
     */
    private static Specification<ProductEntity> nameContains(String name) {
        return (name == null || name.isEmpty()) ? null :
                (root, query, builder) -> builder.like(
                        builder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%");
    }

    /**
     * Filter by SKU (case-insensitive partial match)
     */
    private static Specification<ProductEntity> skuContains(String sku) {
        return (sku == null || sku.isEmpty()) ? null :
                (root, query, builder) -> builder.like(
                        builder.lower(root.get("sku")),
                        "%" + sku.toLowerCase() + "%");
    }

    /**
     * Filter by product status
     */
    private static Specification<ProductEntity> hasStatus(ProductStatus status) {
        return status == null ? null :
                (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    /**
     * Filter by price range
     */
    private static Specification<ProductEntity> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return null;
        }

        if (minPrice != null && maxPrice != null) {
            return (root, query, builder) -> builder.between(root.get("price"), minPrice, maxPrice);
        }

        if (minPrice != null) {
            return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("price"), minPrice);
        }

        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    /**
     * Filter by stock quantity range
     */
    private static Specification<ProductEntity> stockBetween(Integer minStock, Integer maxStock) {
        if (minStock == null && maxStock == null) {
            return null;
        }

        if (minStock != null && maxStock != null) {
            return (root, query, builder) -> builder.between(root.get("stockQuantity"), minStock, maxStock);
        }

        if (minStock != null) {
            return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("stockQuantity"), minStock);
        }

        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("stockQuantity"), maxStock);
    }

    /**
     * General search across name, description, and SKU
     */
    private static Specification<ProductEntity> generalSearch(String searchTerm) {
        return (searchTerm == null || searchTerm.isEmpty()) ? null :
                (root, query, builder) -> builder.or(
                        builder.like(builder.lower(root.get("name")), "%" + searchTerm.toLowerCase() + "%"),
                        builder.like(builder.lower(root.get("description")), "%" + searchTerm.toLowerCase() + "%"),
                        builder.like(builder.lower(root.get("sku")), "%" + searchTerm.toLowerCase() + "%")
                );
    }
} 