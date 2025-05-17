package com.alier.ecommercecore.annotations;

import java.lang.annotation.*;

/**
 * Marks a class as a Use Case in clean/hexagonal architecture.
 * Use cases represent application-specific business rules and orchestrate the flow
 * of data to and from entities, and direct those entities to use their critical
 * business rules to achieve the goals of the use case.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseCase {

    /**
     * Optional description of the use case's responsibility.
     */
    String description() default "";

    /**
     * Indicates whether this use case requires authentication.
     */
    boolean requiresAuthentication() default true;
} 