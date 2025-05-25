package com.alier.ecommerceproductservice.application.usecase;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommercewebcore.rest.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorMessages;

@UseCase(description = "Retrieves all image URLs for a product.")
@RequiredArgsConstructor
@Slf4j
@Service
public class GetProductImagesUseCase {

    private final ProductRepository productRepository;
//    private final ProductCacheService cacheService; // Optional: could check cache first

    @Transactional(readOnly = true)
    public List<String> execute(GetProductImagesInput input) {
        return new GetProductImagesUseCaseHandler().execute(input);
    }

    public record GetProductImagesInput(UUID productId) {
    }

    private class GetProductImagesUseCaseHandler extends UseCaseHandler<GetProductImagesInput, List<String>> {

        @Override
        protected void validate(GetProductImagesInput input) throws BusinessException {
            if (input.productId() == null) {
                throw new BusinessException(GlobalErrorCode.VALIDATION_ERROR, "Product ID cannot be null.");
            }
        }

        @Override
        protected List<String> handle(GetProductImagesInput input) throws BusinessException {
            log.debug("Fetching image URLs for product ID: {}", input.productId());

            // Optional: Check cache first
            // List<String> cachedImageUrls = cacheService.getProductImageUrls(input.getProductId());
            // if (cachedImageUrls != null) {
            //     log.info("Image URLs for product {} retrieved from cache.", input.getProductId());
            //     return cachedImageUrls;
            // }

            Product product = productRepository.findById(input.productId())
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", input.productId());
                        return new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, ProductErrorMessages.PRODUCT_NOT_FOUND_BY_ID);
                    });

            List<String> imageUrls = product.getImageUrls();
            // Optional: cacheService.cacheProductImageUrls(input.getProductId(), imageUrls);

            log.info("Retrieved {} image URLs for product {}.", imageUrls.size(), input.productId());
            return imageUrls;
        }

        @Override
        protected void onSuccess(GetProductImagesInput input, List<String> result) {
            log.info("Successfully retrieved image URLs for product ID: {}", input.productId());
        }

        @Override
        protected void onError(GetProductImagesInput input, Exception exception) {
            log.error("Error retrieving image URLs for product ID: {}. Error: {}",
                    input.productId(), exception.getMessage(), exception);
        }
    }
} 