package com.alier.ecommercecore.annotations;

import java.lang.annotation.*;

/**
 * Marks a class as a Domain Service in the context of Domain-Driven Design.
 * Domain Services implement domain logic that doesn't naturally fit within
 * an entity or value object. They work with multiple domain objects and represent
 * domain concepts and processes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DomainService {

    /**
     * Optional description of the domain service's responsibility.
     */
    String description() default "";
} 