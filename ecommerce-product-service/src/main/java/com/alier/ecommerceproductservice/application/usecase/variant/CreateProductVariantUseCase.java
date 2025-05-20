package com.alier.ecommerceproductservice.application.usecase.variant;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.application.dto.ProductVariantRequest;
import com.alier.ecommerceproductservice.application.dto.ProductVariantResponse;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.model.ProductVariant;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.domain.repository.ProductVariantRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for creating product variants.
 */
@UseCase(description = "Create a new product variant")
@RequiredArgsConstructor
@Slf4j
@Service
public class CreateProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Input data for the use case
     */
    @Data
    @Builder
    public static class InputData {
        private final UUID productId;
        private final ProductVariantRequest request;
    }

    /**
     * Creates a new product variant for a product
     *
     * @param productId The ID of the product to create the variant for
     * @param request   The variant creation request
     * @return The created product variant
     */
    @Transactional
    public ProductVariantResponse execute(UUID productId, ProductVariantRequest request) {
        log.debug("Creating product variant for product ID: {}, SKU: {}", productId, request.getSku());
        return new CreateProductVariantHandler().execute(InputData.builder()
                .productId(productId)
                .request(request)
                .build());
    }

    /**
     * Handler for creating a product variant
     */
    @RequiredArgsConstructor
    private class CreateProductVariantHandler extends UseCaseHandler<InputData, ProductVariantResponse> {

        @Override
        protected ProductVariantResponse handle(InputData input) throws BusinessException {
            UUID productId = input.getProductId();
            ProductVariantRequest request = input.getRequest();

            // Check if the variant SKU already exists
            if (productVariantRepository.existsBySku(request.getSku())) {
                throw new ProductException.ProductVariantSkuAlreadyExistsException(request.getSku());
            }

            // Verify product exists
            if (!productRepository.existsById(productId)) {
                throw new ProductException.ProductNotFoundException(productId);
            }

            // Create domain variant
            ProductVariant variant = ProductVariant.create(
                    productId,
                    request.getName(),
                    request.getSku(),
                    request.getPrice(),
                    request.getStockQuantity(),
                    request.getAttributes()
            );

            // Save the variant
            ProductVariant savedVariant = productVariantRepository.save(variant);

            // Return the response
            return ProductVariantResponse.fromDomain(savedVariant);
        }
    }
} 