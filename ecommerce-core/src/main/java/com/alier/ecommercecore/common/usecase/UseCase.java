package com.alier.ecommercecore.common.usecase;

/**
 * Base interface for all use cases in the system.
 * Represents an application-specific business rule.
 *
 * @param <I> Input type (request/command)
 * @param <O> Output type (response/result)
 */
public interface UseCase<I, O> {

    /**
     * Executes the use case with the given input.
     *
     * @param input the input data required to execute the use case
     * @return the output data resulting from the use case execution
     */
    O execute(I input);
} 