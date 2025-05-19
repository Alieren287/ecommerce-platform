package com.alier.ecommerceproductservice.infrastructure.cache;

import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for caching product data in Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:";
    private static final String PRODUCT_BY_SKU_CACHE_KEY_PREFIX = "product:sku:";
    private static final long CACHE_TTL_HOURS = 24;

    private final RedisTemplate<String, ProductDTO> redisTemplate;

    /**
     * Caches a product DTO.
     *
     * @param productDTO the product DTO to cache
     */
    public void cacheProduct(ProductDTO productDTO) {
        try {
            String idKey = PRODUCT_CACHE_KEY_PREFIX + productDTO.getId();
            String skuKey = PRODUCT_BY_SKU_CACHE_KEY_PREFIX + productDTO.getSku();

            redisTemplate.opsForValue().set(idKey, productDTO, CACHE_TTL_HOURS, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(skuKey, productDTO, CACHE_TTL_HOURS, TimeUnit.HOURS);

            log.debug("Cached product with ID: {}, SKU: {}", productDTO.getId(), productDTO.getSku());
        } catch (Exception e) {
            log.error("Error caching product: {}", e.getMessage(), e);
        }
    }

    /**
     * Gets a product from cache by ID.
     *
     * @param id the product ID
     * @return an Optional containing the product DTO if found in cache, empty otherwise
     */
    public Optional<ProductDTO> getProductById(UUID id) {
        try {
            String key = PRODUCT_CACHE_KEY_PREFIX + id;
            ProductDTO cachedProduct = redisTemplate.opsForValue().get(key);

            if (cachedProduct != null) {
                log.debug("Cache hit for product ID: {}", id);
                return Optional.of(cachedProduct);
            } else {
                log.debug("Cache miss for product ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error retrieving product from cache: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Gets a product from cache by SKU.
     *
     * @param sku the product SKU
     * @return an Optional containing the product DTO if found in cache, empty otherwise
     */
    public Optional<ProductDTO> getProductBySku(String sku) {
        try {
            String key = PRODUCT_BY_SKU_CACHE_KEY_PREFIX + sku;
            ProductDTO cachedProduct = redisTemplate.opsForValue().get(key);

            if (cachedProduct != null) {
                log.debug("Cache hit for product SKU: {}", sku);
                return Optional.of(cachedProduct);
            } else {
                log.debug("Cache miss for product SKU: {}", sku);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error retrieving product from cache: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Evicts a product from cache.
     *
     * @param id the product ID
     */
    public void evictProduct(UUID id) {
        try {
            // First get the product to find its SKU
            String idKey = PRODUCT_CACHE_KEY_PREFIX + id;
            ProductDTO cachedProduct = redisTemplate.opsForValue().get(idKey);
            redisTemplate.delete(idKey);
            // If found in cache, also delete by SKU
            if (cachedProduct != null) {
                // Delete by ID
                String skuKey = PRODUCT_BY_SKU_CACHE_KEY_PREFIX + cachedProduct.getSku();
                redisTemplate.delete(skuKey);
                log.debug("Evicted product with ID: {}, SKU: {} from cache", id, cachedProduct.getSku());
            } else {
                log.debug("Product with ID: {} not found in cache", id);
            }
        } catch (Exception e) {
            log.error("Error evicting product from cache: {}", e.getMessage(), e);
        }
    }
} 