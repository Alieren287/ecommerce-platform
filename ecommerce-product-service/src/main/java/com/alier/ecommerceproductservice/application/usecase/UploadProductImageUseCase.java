package com.alier.ecommerceproductservice.application.usecase;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import com.alier.ecommerceproductservice.application.service.ImageStorageService;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorMessages;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.infrastructure.cache.ProductCacheService;
import com.alier.ecommerceproductservice.infrastructure.messaging.ProductEventPublisher;
import com.alier.ecommerceproductservice.infrastructure.search.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@UseCase(description = "Uploads an image for a product and associates its URL.")
@RequiredArgsConstructor
@Slf4j
@Service
public class UploadProductImageUseCase {

    // Define allowed image MIME types
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;
    private final ProductEventPublisher eventPublisher;
    private final ProductCacheService cacheService;
    private final ProductSearchService searchService;

    @Transactional
    public ProductDTO execute(UploadProductImageInput input) {
        return new UploadProductImageUseCaseHandler().execute(input);
    }


    public record UploadProductImageInput(UUID productId, MultipartFile imageFile) {
    }

    private class UploadProductImageUseCaseHandler extends UseCaseHandler<UploadProductImageInput, ProductDTO> {

        @Override
        protected void validate(UploadProductImageInput input) throws ProductException {
            if (input.productId() == null) {
                throw new ProductException(ProductErrorCode.PRODUCT_NAME_NULL, "Product ID cannot be null.");
            }
            if (input.imageFile() == null || input.imageFile().isEmpty()) {
                throw new ProductException(ProductErrorCode.PRODUCT_IMAGE_URL_EMPTY, "Image file cannot be empty.");
            }
            if (input.imageFile().getSize() > MAX_FILE_SIZE_BYTES) {
                throw new ProductException(ProductErrorCode.PRODUCT_IMAGE_URL_EMPTY, "Image file size exceeds the limit of " + (MAX_FILE_SIZE_BYTES / (1024 * 1024)) + "MB.");
            }
            String contentType = input.imageFile().getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                throw new ProductException(ProductErrorCode.PRODUCT_IMAGE_URL_EMPTY, "Invalid image file type. Allowed types: " + ALLOWED_IMAGE_TYPES);
            }
        }

        @Override
        protected ProductDTO handle(UploadProductImageInput input) throws ProductException {
            log.debug("Attempting to upload image for product ID: {}", input.productId());

            Product product = productRepository.findById(input.productId())
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", input.productId());
                        return new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, ProductErrorMessages.PRODUCT_NOT_FOUND_BY_ID);
                    });

            String storedImagePath;
            try {
                storedImagePath = imageStorageService.storeImage(input.productId(), input.imageFile());
            } catch (IOException e) {
                log.error("Failed to store image for product ID {}: {}", input.productId(), e.getMessage(), e);
                throw new ProductException(ProductErrorCode.PRODUCT_UPDATE_FAILED,
                        ProductErrorMessages.PRODUCT_UPDATE_OPERATION_FAILED);
            }

            // Get the full public URL for the stored image path
            String publicImageUrl = imageStorageService.getPublicUrl(storedImagePath);
            product.addImageUrl(publicImageUrl);

            Product updatedProduct = productRepository.save(product);
            ProductDTO productDTO = ProductDTO.fromDomain(updatedProduct);

            cacheService.cacheProduct(productDTO);
            searchService.indexProduct(updatedProduct);
            eventPublisher.publishProductUpdatedEvent(updatedProduct); // Consider specific ProductImageAddedEvent

            log.info("Image {} successfully uploaded and associated with product {}. Public URL: {}",
                    input.imageFile().getOriginalFilename(), input.productId(), publicImageUrl);
            return productDTO;
        }

        @Override
        protected void onSuccess(UploadProductImageInput input, ProductDTO result) {
            log.info("Successfully processed image upload for product ID: {}", input.productId());

        }

        @Override
        protected void onError(UploadProductImageInput input, Exception exception) {
            log.error("Error processing image upload for product ID: {}. File: {}. Error: {}",
                    input.productId(),
                    (input.imageFile() != null ? input.imageFile().getOriginalFilename() : "null"),
                    exception.getMessage(), exception);
        }
    }
} 