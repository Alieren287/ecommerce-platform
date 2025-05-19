package com.alier.ecommerceproductservice.domain.repository;

import com.alier.ecommerceproductservice.application.dto.ProductFilterRequest;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Product domain object.
 * This is a domain interface (port) that will be implemented by an adapter
 * in the infrastructure layer.
 */
public interface ProductRepository {

    /**
     * Save a product
     *
     * @param product the product to save
     * @return the saved product
     */
    Product save(Product product);

    /**
     * Save multiple products in a batch operation
     *
     * @param products the list of products to save
     * @return the list of saved products
     */
    List<Product> saveAll(List<Product> products);

    /**
     * Find a product by its ID
     *
     * @param id the product ID
     * @return an Optional containing the product if found, empty otherwise
     */
    Optional<Product> findById(UUID id);

    /**
     * Find a product by its SKU
     *
     * @param sku the product SKU
     * @return an Optional containing the product if found, empty otherwise
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find all products with a specific status
     *
     * @param status the product status
     * @return list of products with the given status
     */
    List<Product> findByStatus(ProductStatus status);

    /**
     * Find all products
     *
     * @return list of all products
     */
    List<Product> findAll();

    /**
     * Find all products with pagination
     *
     * @param pageable pagination information
     * @return page of products
     */
    Page<Product> findAll(Pageable pageable);

    /**
     * Find products by status with pagination
     *
     * @param status   the product status
     * @param pageable pagination information
     * @return page of products
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * Find products by filtering criteria with pagination
     *
     * @param filterRequest the filtering criteria
     * @param pageable      pagination information
     * @return page of products matching the criteria
     */
    Page<Product> findByFilters(ProductFilterRequest filterRequest, Pageable pageable);

    /**
     * Simple search across name, description and SKU with pagination
     *
     * @param searchTerm the term to search for
     * @param pageable   pagination information
     * @return page of products matching the search
     */
    Page<Product> search(String searchTerm, Pageable pageable);

    /**
     * Find products with price in a specific range
     *
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     * @param pageable pagination information
     * @return page of products within the price range
     */
    Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Delete a product
     *
     * @param id the ID of the product to delete
     */
    void deleteById(UUID id);

    /**
     * Check if a product with the given SKU exists
     *
     * @param sku the product SKU to check
     * @return true if a product with the SKU exists, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Find products by ID list
     *
     * @param productIds list of product IDs to find
     * @return list of products matching the IDs
     */
    List<Product> findById(List<UUID> productIds);

    /**
     * Get current count of products
     *
     * @return product count
     */
    long count();
} 