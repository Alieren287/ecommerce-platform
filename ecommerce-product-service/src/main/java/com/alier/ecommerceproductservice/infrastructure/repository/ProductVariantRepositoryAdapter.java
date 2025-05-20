package com.alier.ecommerceproductservice.infrastructure.repository;

import com.alier.ecommerceproductservice.domain.model.ProductVariant;
import com.alier.ecommerceproductservice.domain.repository.ProductVariantRepository;
import com.alier.ecommerceproductservice.infrastructure.repository.entity.ProductVariantEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the domain ProductVariantRepository interface using Spring Data JPA.
 * This is an adapter in hexagonal architecture that connects the domain layer
 * to the data persistence infrastructure.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductVariantRepositoryAdapter implements ProductVariantRepository {

    private final JpaProductVariantRepository jpaRepository;

    @Override
    public ProductVariant save(ProductVariant productVariant) {
        ProductVariantEntity entity = ProductVariantEntity.fromDomain(productVariant);
        ProductVariantEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<ProductVariant> saveAll(List<ProductVariant> productVariants) {
        List<ProductVariantEntity> entities = productVariants.stream()
                .map(ProductVariantEntity::fromDomain)
                .collect(Collectors.toList());

        return jpaRepository.saveAll(entities).stream()
                .map(ProductVariantEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductVariant> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(ProductVariantEntity::toDomain);
    }

    @Override
    public Optional<ProductVariant> findBySku(String sku) {
        return jpaRepository.findBySku(sku)
                .map(ProductVariantEntity::toDomain);
    }

    @Override
    public List<ProductVariant> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId).stream()
                .map(ProductVariantEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductVariant> findByProductId(UUID productId, Pageable pageable) {
        return jpaRepository.findByProductId(productId, pageable)
                .map(ProductVariantEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }
} 