package com.alier.ecommerceproductservice.infrastructure.repository.entity;

import com.alier.ecommerceproductservice.domain.model.ProductVariant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JPA entity for ProductVariant.
 */
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantEntity {
    private static final Logger log = LoggerFactory.getLogger(ProductVariantEntity.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "product_id", insertable = false, updatable = false)
    private UUID productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "attributes", columnDefinition = "TEXT")
    private String attributesJson;

    @Transient
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Creates a JPA entity from a domain entity
     */
    public static ProductVariantEntity fromDomain(ProductVariant productVariant) {
        String attributesJson = null;
        try {
            attributesJson = objectMapper.writeValueAsString(productVariant.getAttributes());
        } catch (JsonProcessingException e) {
            log.error("Error serializing attributes to JSON", e);
            attributesJson = "{}";
        }
        
        return ProductVariantEntity.builder()
                .id(productVariant.getId())
                .productId(productVariant.getProductId())
                .name(productVariant.getName())
                .sku(productVariant.getSku())
                .price(productVariant.getPrice())
                .stockQuantity(productVariant.getStockQuantity())
                .attributesJson(attributesJson)
                .attributes(productVariant.getAttributes())
                .createdAt(productVariant.getCreatedAt())
                .updatedAt(productVariant.getUpdatedAt())
                .build();
    }

    /**
     * Creates a domain entity from this JPA entity
     */
    public ProductVariant toDomain() {
        Map<String, Object> attributesMap = new HashMap<>();
        if (attributesJson != null && !attributesJson.isEmpty()) {
            try {
                attributesMap = objectMapper.readValue(attributesJson, 
                    new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                log.error("Error deserializing JSON to attributes map", e);
            }
        }
        
        return ProductVariant.builder()
                .id(this.id)
                .productId(this.productId)
                .name(this.name)
                .sku(this.sku)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .attributes(attributesMap)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
    
    @PostLoad
    private void loadAttributes() {
        if (attributesJson != null && !attributesJson.isEmpty()) {
            try {
                attributes = objectMapper.readValue(attributesJson, 
                    new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                log.error("Error deserializing JSON to attributes map", e);
                attributes = new HashMap<>();
            }
        } else {
            attributes = new HashMap<>();
        }
    }
    
    @PrePersist
    @PreUpdate
    private void saveAttributes() {
        try {
            this.attributesJson = objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            log.error("Error serializing attributes to JSON", e);
            this.attributesJson = "{}";
        }
    }
} 