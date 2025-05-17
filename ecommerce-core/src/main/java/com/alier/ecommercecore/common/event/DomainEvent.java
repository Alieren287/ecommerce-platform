package com.alier.ecommercecore.common.event;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events in the system.
 * Domain events represent something that has happened in the domain
 * that domain experts care about.
 */
public interface DomainEvent {

    /**
     * Returns the type of the domain event.
     *
     * @return the event type as string
     */
    String getType();

    /**
     * Returns the timestamp when the event occurred.
     *
     * @return the timestamp of when the event occurred
     */
    LocalDateTime getOccurredOn();
} 