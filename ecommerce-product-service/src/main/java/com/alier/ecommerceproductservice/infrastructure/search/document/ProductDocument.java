package com.alier.ecommerceproductservice.infrastructure.search.document;

import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Elasticsearch document for product search.
 */
@Document(indexName = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    @Field(type = FieldType.Keyword)
    private ProductStatus status;

    @Field(type = FieldType.Keyword)
    private List<String> imageUrls;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    /**
     * Convert from domain model to Elasticsearch document
     *
     * @param product Domain model
     * @return Elasticsearch document
     */
    public static ProductDocument fromDomain(Product product) {
        return ProductDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice().doubleValue())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .imageUrls(product.getImageUrls())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Convert to domain model
     *
     * @return Domain model
     */
    public Product toDomain() {
        Product product = Product.builder()
                .id(UUID.fromString(id))
                .name(name)
                .description(description)
                .sku(sku)
                .price(price != null ? new BigDecimal(price.toString()) : null)
                .stockQuantity(stockQuantity)
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