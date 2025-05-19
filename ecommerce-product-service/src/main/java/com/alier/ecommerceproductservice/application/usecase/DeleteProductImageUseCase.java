package com.alier.ecommerceproductservice.application.usecase;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import com.alier.ecommerceproductservice.application.service.ImageStorageService;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.infrastructure.cache.ProductCacheService;
import com.alier.ecommerceproductservice.infrastructure.messaging.ProductEventPublisher;
import com.alier.ecommerceproductservice.infrastructure.search.ProductSearchService;
import com.alier.ecommercewebcore.rest.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@UseCase(description = "Deletes an image from a product (both URL association and from storage).")
@RequiredArgsConstructor
@Slf4j
@Service
public class DeleteProductImageUseCase {

    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;
    private final ProductEventPublisher eventPublisher;
    private final ProductCacheService cacheService;
    private final ProductSearchService searchService;

    @Transactional
    public ProductDTO execute(DeleteProductImageInput input) {
        return new DeleteProductImageUseCaseHandler().execute(input);
    }

    /**
     * @param imageUrlToDelete Full public URL of the image to delete
     */
    public record DeleteProductImageInput(UUID productId, String imageUrlToDelete) {
    }

    private class DeleteProductImageUseCaseHandler extends UseCaseHandler<DeleteProductImageInput, ProductDTO> {

        @Override
        protected void validate(DeleteProductImageInput input) throws BusinessException {
            if (input.productId() == null) {
                throw new BusinessException(GlobalErrorCode.VALIDATION_ERROR, "Product ID cannot be null.");
            }
            if (input.imageUrlToDelete() == null || input.imageUrlToDelete().trim().isEmpty()) {
                throw new BusinessException(GlobalErrorCode.VALIDATION_ERROR, "Image URL to delete cannot be empty.");
            }
            try {
                new URI(input.imageUrlToDelete()); // Validate if it's a valid URI
            } catch (URISyntaxException e) {
                throw new BusinessException(GlobalErrorCode.VALIDATION_ERROR, "Invalid image URL format.");
            }
        }

        @Override
        protected ProductDTO handle(DeleteProductImageInput input) throws BusinessException {
            log.debug("Attempting to delete image URL {} from product ID: {}", input.imageUrlToDelete(), input.productId());

            Product product = productRepository.findById(input.productId())
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", input.productId());
                        return new ProductException.ProductNotFoundException(input.productId());
                    });

            String imageUrlToDelete = input.imageUrlToDelete();
            boolean wasAssociated = product.getImageUrls().contains(imageUrlToDelete);

            if (!wasAssociated) {
                log.warn("Image URL {} was not associated with product {}. No database change needed.", imageUrlToDelete, input.productId());
                // Still attempt to delete from storage if it's a direct file path, though less likely if not in DB.
                // This part is tricky: the imageUrlToDelete is a public URL.
                // We need to convert it to the internal storage path that imageStorageService.deleteImage expects.
                // This requires knowledge of how getPublicUrl constructs the URL.
                // For LocalImageStorageService, public URL is /api/v1/product-images/{productId}/{filename}
                // So, the imagePath for the service is {productId}/{filename}
                try {
                    URI uri = new URI(imageUrlToDelete);
                    String path = uri.getPath(); // e.g., /api/v1/product-images/product-id/filename.jpg
                    // Assuming the imageStorageService.publicPathSegment is "product-images"
                    // and the base API path for images is /api/v1/product-images/
                    String basePath = "/api/v1/" + imageStorageService.getPublicUrl("").substring(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString().length() + "/api/v1/".length()).split("/")[0] + "/";
                    String internalPath = path.startsWith(basePath) ? path.substring(basePath.length()) : null;

                    if (internalPath != null) {
                        imageStorageService.deleteImage(internalPath);
                        log.info("Attempted to delete image from storage (not found in DB): {}", internalPath);
                    }
                } catch (IOException | URISyntaxException | ArrayIndexOutOfBoundsException e) {
                    log.warn("Could not attempt to delete unassociated image {} from storage: {}", imageUrlToDelete, e.getMessage());
                }
                // Return the product as is, since no DB change occurred.
                return ProductDTO.fromDomain(product);
            }

            // Image URL is associated, remove it from the product
            product.removeImageUrl(imageUrlToDelete);
            Product updatedProduct = productRepository.save(product);

            // Attempt to delete from storage
            try {
                // Convert public URL to storable path for deletion
                URI uri = new URI(imageUrlToDelete);
                String path = uri.getPath();
                // This logic needs to be robust and ideally centralized if URL structures are complex
                // For LocalImageStorageService path is like "<productId>/<filename>"
                // public URL is something like http://host/api/v1/product-images/<productId>/<filename>
                // We need to extract "<productId>/<filename>"
                String[] segments = path.split("/");
                if (segments.length >= 2) { // Expecting at least /.../{productId}/{filename}
                    String internalPath = segments[segments.length - 2] + "/" + segments[segments.length - 1];
                    imageStorageService.deleteImage(internalPath); // Use the extracted internal path
                    log.info("Successfully deleted image from storage: {}", internalPath);
                }
            } catch (IOException | URISyntaxException e) {
                log.error("Failed to delete image {} from storage for product {}: {}. DB record removed.",
                        imageUrlToDelete, input.productId(), e.getMessage(), e);
                // Non-critical error for the use case's success, but should be monitored.
            }

            ProductDTO productDTO = ProductDTO.fromDomain(updatedProduct);
            cacheService.cacheProduct(productDTO);
            searchService.indexProduct(updatedProduct);
            eventPublisher.publishProductUpdatedEvent(updatedProduct); // Consider specific ProductImageDeletedEvent

            log.info("Image URL {} successfully disassociated from product {} and deletion from storage attempted.", imageUrlToDelete, input.productId());
            return productDTO;
        }

        @Override
        protected void onSuccess(DeleteProductImageInput input, ProductDTO result) {
            log.info("Successfully processed image deletion request for product ID: {}, image URL: {}", input.productId(), input.imageUrlToDelete());
        }

        @Override
        protected void onError(DeleteProductImageInput input, Exception exception) {
            log.error("Error processing image deletion for product ID: {}, image URL: {}. Error: {}",
                    input.productId(), input.imageUrlToDelete(), exception.getMessage(), exception);
        }
    }
} 