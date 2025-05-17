package com.alier.ecommercecore.common.usecase;

import com.alier.ecommercecore.common.exception.BaseBusinessException;
import com.alier.ecommercecore.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Base abstract class for all use case handlers.
 * Provides common infrastructure for use case execution, including
 * logging, transaction management, and exception handling.
 *
 * @param <I> Input type (request/command)
 * @param <O> Output type (response/result)
 */
@Slf4j
public abstract class UseCaseHandler<I, O> implements UseCase<I, O> {

    /**
     * Template method that defines the skeleton of the use case execution algorithm.
     * It delegates to hook methods that subclasses must implement.
     *
     * @param input the input data
     * @return the output data
     */
    @Override
    public final O execute(I input) {
        try {
            log.debug("Executing use case: {}", this.getClass().getSimpleName());

            // Pre-execution validation
            validate(input);

            // Execute the use case
            O result = handle(input);

            // Post-execution processing
            onSuccess(input, result);

            log.debug("Use case execution completed: {}", this.getClass().getSimpleName());
            return result;
        } catch (BaseBusinessException e) {
            log.warn("Business exception occurred while executing use case {}: {}",
                    this.getClass().getSimpleName(), e.getMessage());
            onError(input, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while executing use case {}: {}",
                    this.getClass().getSimpleName(), e.getMessage(), e);
            onError(input, e);
            throw e;
        }
    }

    /**
     * Hook method for input validation.
     * Subclasses should override this method to perform input validation.
     *
     * @param input the input data to validate
     * @throws BusinessException if validation fails
     */
    protected void validate(I input) throws BusinessException {
        // Default implementation does nothing
    }

    /**
     * Hook method that contains the core use case logic.
     * Subclasses must implement this method.
     *
     * @param input the validated input data
     * @return the output data
     * @throws BusinessException if a business rule is violated
     */
    protected abstract O handle(I input) throws BusinessException;

    /**
     * Hook method called after successful execution.
     * Subclasses may override this method to perform post-execution processing.
     *
     * @param input  the input data
     * @param result the output data
     */
    protected void onSuccess(I input, O result) {
        // Default implementation does nothing
    }

    /**
     * Hook method called when an error occurs.
     * Subclasses may override this method to perform error handling.
     *
     * @param input     the input data
     * @param exception the exception that occurred
     */
    protected void onError(I input, Exception exception) {
        // Default implementation does nothing
    }
} 