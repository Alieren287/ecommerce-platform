package com.alier.ecommerceproductservice.infrastructure.repository.entity;

import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * JPA entity for Product.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    // Using ElementCollection for a simple one-to-many relationship with basic types
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_image_urls",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductVariantEntity> variants = new HashSet<>();

    /**
     * Creates a JPA entity from a domain entity
     */
    public static ProductEntity fromDomain(Product product) {
        ProductEntity entity = ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

        // Set image URLs
        if (product.getImageUrls() != null) {
            entity.setImageUrls(new ArrayList<>(product.getImageUrls()));
        }

        return entity;
    }

    /**
     * Converts this JPA entity to a domain entity
     */
    public Product toDomain() {
        Product product = Product.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .sku(sku)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Add image URLs if any exist
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                product.addImageUrl(url);
            }
        }

        return product;
    }
} 