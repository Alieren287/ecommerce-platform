package com.alier.ecommerceproductservice.application.usecase.variant;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.dto.PaginatedResponse;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.application.dto.ProductVariantResponse;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.model.ProductVariant;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.domain.repository.ProductVariantRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorMessages;

/**
 * Use case for retrieving product variants.
 */
@UseCase(description = "Retrieve product variant information")
@RequiredArgsConstructor
@Slf4j
@Service
public class GetProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Gets a product variant by ID
     *
     * @param variantId The ID of the variant
     * @return The product variant response
     */
    @Transactional(readOnly = true)
    public ProductVariantResponse getById(UUID variantId) {
        log.debug("Getting product variant with ID: {}", variantId);
        return new GetByIdHandler().execute(variantId);
    }

    /**
     * Gets a product variant by SKU
     *
     * @param sku The SKU of the variant
     * @return The product variant response
     */
    @Transactional(readOnly = true)
    public ProductVariantResponse getBySku(String sku) {
        log.debug("Getting product variant with SKU: {}", sku);
        return new GetBySkuHandler().execute(sku);
    }

    /**
     * Gets all variants for a product
     *
     * @param productId The ID of the product
     * @return List of product variant responses
     */
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getByProductId(UUID productId) {
        log.debug("Getting variants for product ID: {}", productId);
        return new GetByProductIdHandler().execute(productId);
    }

    /**
     * Input data for paginated query
     */
    @Data
    @Builder
    public static class PaginatedQueryInput {
        private final UUID productId;
        private final int page;
        private final int size;
        private final String sortBy;
        private final String sortDir;
    }

    /**
     * Gets all variants for a product with pagination
     *
     * @param productId The ID of the product
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction ("asc" or "desc")
     * @return Paginated response of product variants
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductVariantResponse> getByProductIdPaged(
            UUID productId, int page, int size, String sortBy, String sortDir) {
        log.debug("Getting paginated variants for product ID: {}, page: {}, size: {}", productId, page, size);
        
        return new GetByProductIdPagedHandler().execute(PaginatedQueryInput.builder()
                .productId(productId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build());
    }

    /**
     * Handler for getting a product variant by ID
     */
    private class GetByIdHandler extends UseCaseHandler<UUID, ProductVariantResponse> {
        @Override
        protected ProductVariantResponse handle(UUID variantId) throws BusinessException {
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND, ProductErrorMessages.PRODUCT_VARIANT_NOT_FOUND_BY_ID));
            
            return ProductVariantResponse.fromDomain(variant);
        }
    }
    
    /**
     * Handler for getting a product variant by SKU
     */
    private class GetBySkuHandler extends UseCaseHandler<String, ProductVariantResponse> {
        @Override
        protected ProductVariantResponse handle(String sku) throws BusinessException {
            ProductVariant variant = productVariantRepository.findBySku(sku)
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND, ProductErrorMessages.PRODUCT_VARIANT_NOT_FOUND_BY_SKU));
            
            return ProductVariantResponse.fromDomain(variant);
        }
    }
    
    /**
     * Handler for getting all variants for a product
     */
    private class GetByProductIdHandler extends UseCaseHandler<UUID, List<ProductVariantResponse>> {
        @Override
        protected void validate(UUID productId) throws BusinessException {
            if (!productRepository.existsById(productId)) {
                throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, ProductErrorMessages.PRODUCT_NOT_FOUND_BY_ID);
            }
        }

        @Override
        protected List<ProductVariantResponse> handle(UUID productId) throws BusinessException {
            List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
            return variants.stream()
                    .map(ProductVariantResponse::fromDomain)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Handler for getting paginated variants for a product
     */
    private class GetByProductIdPagedHandler 
            extends UseCaseHandler<PaginatedQueryInput, PaginatedResponse<ProductVariantResponse>> {

        @Override
        protected void validate(PaginatedQueryInput input) throws BusinessException {
            // Verify that the product exists
            if (!productRepository.existsById(input.getProductId())) {
                throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, ProductErrorMessages.PRODUCT_NOT_FOUND_BY_ID);
            }

        }

        @Override
        protected PaginatedResponse<ProductVariantResponse> handle(PaginatedQueryInput input) 
                throws BusinessException {
            // Set up sorting
            Sort sort = input.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                    Sort.by(input.getSortBy()).ascending() : 
                    Sort.by(input.getSortBy()).descending();
            
            // Create pageable object
            Pageable pageable = PageRequest.of(input.getPage(), input.getSize(), sort);
            
            // Get paginated variants
            Page<ProductVariant> variantPage = productVariantRepository.findByProductId(input.getProductId(), pageable);
            
            // Map to DTOs
            List<ProductVariantResponse> variantDTOs = variantPage.getContent().stream()
                    .map(ProductVariantResponse::fromDomain)
                    .collect(Collectors.toList());
            
            // Create paginated response
            return new PaginatedResponse<>(
                    variantDTOs,
                    variantPage.getNumber(),
                    variantPage.getSize(),
                    variantPage.getTotalElements(),
                    variantPage.getTotalPages()
            );
        }
    }
} 