package com.alier.ecommerceproductservice.domain.service;

import com.alier.ecommercecore.annotations.DomainService;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Domain service for products.
 * Contains business logic that doesn't naturally fit in the Product entity.
 */
@DomainService
@Service
@RequiredArgsConstructor
public class ProductDomainService {

    private final ProductRepository productRepository;

    /**
     * Check if a product SKU is available for use.
     *
     * @param sku the SKU to check
     * @return true if the SKU is available (not used by any product), false otherwise
     */
    public boolean isSkuExists(String sku) {
        return productRepository.existsBySku(sku);
    }

    /**
     * Applies a price discount to a product.
     *
     * @param product            the product to apply the discount to
     * @param discountPercentage the discount percentage (0-100)
     * @return the product with the discounted price
     */
    public Product applyDiscount(Product product, int discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw BusinessException.validation(ProductErrorCode.PRODUCT_DISCOUNT_INVALID);
        }

        if (discountPercentage == 0) {
            return product;
        }

        BigDecimal discountFactor = BigDecimal.valueOf((100 - discountPercentage) / 100.0);
        BigDecimal discountedPrice = product.getPrice().multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);

        return product.update(
                product.getName(),
                product.getDescription(),
                discountedPrice,
                product.getStockQuantity()
        );
    }

    /**
     * Replicates a product with a new SKU, creating a copy with zero stock.
     *
     * @param sourceProductId the ID of the product to replicate
     * @param newSku          the SKU for the new product
     * @return the replicated product
     * @throws BusinessException if the product doesn't exist or the SKU is already in use
     */
    public Product replicateProduct(UUID sourceProductId, String newSku) {
        if (isSkuExists(newSku)) {
            throw BusinessException.conflict(ProductErrorCode.PRODUCT_SKU_EXISTS);
        }

        Product sourceProduct = productRepository.findById(sourceProductId)
                .orElseThrow(() -> BusinessException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND));

        Product replica = Product.create(
                sourceProduct.getName(),
                sourceProduct.getDescription(),
                sourceProduct.getPrice(),
                0, // Start with zero stock
                newSku
        );

        if (sourceProduct.getImageUrls() != null && !sourceProduct.getImageUrls().isEmpty()) {
            for (String imageUrl : sourceProduct.getImageUrls()) {
                replica.addImageUrl(imageUrl);
            }
        }

        return replica;
    }
} 