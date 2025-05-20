package com.alier.ecommerceproductservice.application.usecase.variant;

import com.alier.ecommercecore.annotations.UseCase;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.usecase.UseCaseHandler;
import com.alier.ecommerceproductservice.domain.exception.ProductException;
import com.alier.ecommerceproductservice.domain.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for deleting product variants.
 */
@UseCase(description = "Delete an existing product variant")
@RequiredArgsConstructor
@Slf4j
@Service
public class DeleteProductVariantUseCase {

    private final ProductVariantRepository productVariantRepository;

    /**
     * Deletes a product variant
     *
     * @param variantId The ID of the variant to delete
     */
    @Transactional
    public void execute(UUID variantId) {
        log.debug("Deleting product variant with ID: {}", variantId);
        new DeleteVariantHandler().execute(variantId);
    }

    /**
     * Handler for deleting a variant
     */
    private class DeleteVariantHandler extends UseCaseHandler<UUID, Void> {

        @Override
        protected void validate(UUID variantId) throws BusinessException {
            // Check if variant exists
            if (productVariantRepository.findById(variantId).isEmpty()) {
                throw new ProductException.ProductVariantNotFoundException(variantId);
            }

        }

        @Override
        protected Void handle(UUID variantId) throws BusinessException {
            // Delete the variant
            productVariantRepository.deleteById(variantId);
            log.info("Product variant deleted successfully with ID: {}", variantId);

            return null;
        }
    }
} 