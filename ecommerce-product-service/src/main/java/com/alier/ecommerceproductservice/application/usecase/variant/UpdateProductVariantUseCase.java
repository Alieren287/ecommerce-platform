package com.alier.ecommerceproductservice.application.usecase.variant;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.application.dto.ProductVariantRequest;
import com.alier.ecommerceproductservice.application.dto.ProductVariantResponse;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.model.ProductVariant;
import com.alier.ecommerceproductservice.domain.repository.ProductVariantRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for updating product variants.
 */
@UseCase(description = "Update an existing product variant")
@RequiredArgsConstructor
@Slf4j
@Service
public class UpdateProductVariantUseCase {

    private final ProductVariantRepository productVariantRepository;

    /**
     * Updates an existing product variant
     *
     * @param variantId The ID of the variant to update
     * @param request   The variant update request
     * @return The updated product variant
     */
    @Transactional
    public ProductVariantResponse execute(UUID variantId, ProductVariantRequest request) {
        log.debug("Updating product variant with ID: {}", variantId);

        return new UpdateVariantHandler().execute(UpdateInput.builder()
                .variantId(variantId)
                .request(request)
                .build());
    }

    /**
     * Updates the stock quantity of a product variant
     *
     * @param variantId     The ID of the variant
     * @param stockQuantity The new stock quantity
     * @return The updated product variant
     */
    @Transactional
    public ProductVariantResponse updateStock(UUID variantId, Integer stockQuantity) {
        log.debug("Updating stock for product variant with ID: {}, new quantity: {}", variantId, stockQuantity);

        return new UpdateStockHandler().execute(StockUpdateInput.builder()
                .variantId(variantId)
                .stockQuantity(stockQuantity)
                .build());
    }

    /**
     * Input data for updating a variant
     */
    @Data
    @Builder
    public static class UpdateInput {
        private final UUID variantId;
        private final ProductVariantRequest request;
    }

    /**
     * Input data for updating stock
     */
    @Data
    @Builder
    public static class StockUpdateInput {
        private final UUID variantId;
        private final Integer stockQuantity;
    }

    /**
     * Handler for updating a variant
     */
    private class UpdateVariantHandler extends UseCaseHandler<UpdateInput, ProductVariantResponse> {

        @Override
        protected ProductVariantResponse handle(UpdateInput input) throws BusinessException {
            UUID variantId = input.getVariantId();
            ProductVariantRequest request = input.getRequest();

            // Find the variant
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> BusinessException.notFound(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND));

            // Check if variant with the new SKU already exists (if SKU is being changed)
            if (request.getSku() != null && !request.getSku().equals(variant.getSku())) {
                if (productVariantRepository.existsBySku(request.getSku())) {
                    throw BusinessException.conflict(ProductErrorCode.PRODUCT_VARIANT_SKU_EXISTS);
                }
            }

            // Update the variant
            variant = variant.update(
                    request.getName(),
                    request.getPrice(),
                    request.getStockQuantity(),
                    request.getAttributes()
            );

            // Save and return
            ProductVariant savedVariant = productVariantRepository.save(variant);
            return ProductVariantResponse.fromDomain(savedVariant);
        }
    }

    /**
     * Handler for updating stock
     */
    private class UpdateStockHandler extends UseCaseHandler<StockUpdateInput, ProductVariantResponse> {

        @Override
        protected ProductVariantResponse handle(StockUpdateInput input) throws BusinessException {
            UUID variantId = input.getVariantId();
            Integer stockQuantity = input.getStockQuantity();

            // Find the variant
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> BusinessException.notFound(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND));

            // Update stock
            variant = variant.updateStock(stockQuantity);

            // Save and return
            ProductVariant savedVariant = productVariantRepository.save(variant);
            return ProductVariantResponse.fromDomain(savedVariant);
        }
    }
} 