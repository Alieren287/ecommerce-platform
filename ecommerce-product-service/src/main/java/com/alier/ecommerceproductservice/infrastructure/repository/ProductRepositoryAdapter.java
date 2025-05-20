package com.alier.ecommerceproductservice.infrastructure.repository;

import com.alier.ecommerceproductservice.application.dto.ProductFilterRequest;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.infrastructure.repository.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the domain ProductRepository interface using Spring Data JPA.
 * This is an adapter in hexagonal architecture that connects the domain layer
 * to the data persistence infrastructure.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "products")
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaRepository;

    @Override
    @CacheEvict(allEntries = true)
    public Product save(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        ProductEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<Product> saveAll(List<Product> products) {
        List<ProductEntity> entities = products.stream()
                .map(ProductEntity::fromDomain)
                .collect(Collectors.toList());

        return jpaRepository.saveAll(entities).stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "#id", unless = "#result == null")
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(ProductEntity::toDomain);
    }

    @Override
    @Cacheable(key = "#sku", unless = "#result == null")
    public Optional<Product> findBySku(String sku) {
        return jpaRepository.findBySku(sku)
                .map(ProductEntity::toDomain);
    }

    @Override
    @Cacheable(key = "'status_' + #status")
    public List<Product> findByStatus(ProductStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'all'")
    public List<Product> findAll() {
        log.debug("Fetching all products from database");
        return jpaRepository.findAll()
                .stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        log.debug("Fetching page {} of products with size {}", pageable.getPageNumber(), pageable.getPageSize());
        return jpaRepository.findAll(pageable)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Page<Product> findByStatus(ProductStatus status, Pageable pageable) {
        log.debug("Fetching page {} of products with status {}", pageable.getPageNumber(), status);
        return jpaRepository.findByStatus(status, pageable)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Page<Product> findByFilters(ProductFilterRequest filterRequest, Pageable pageable) {
        log.debug("Searching products with filters: {}", filterRequest);
        return jpaRepository.findAll(ProductSpecifications.withFilters(filterRequest), pageable)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Page<Product> search(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);
        return jpaRepository.search(searchTerm, pageable)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching products with price between {} and {}", minPrice, maxPrice);
        return jpaRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(ProductEntity::toDomain);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public boolean existsById(UUID productId) {
        return jpaRepository.existsById(productId);
    }

    @Override
    public List<Product> findById(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        log.debug("Finding products by IDs, count: {}", productIds.size());
        return jpaRepository.findAllById(productIds)
                .stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }
} 