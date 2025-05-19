package com.alier.ecommerceproductservice.infrastructure.messaging;

import com.alier.ecommercecore.common.logging.CorrelationContext;
import com.alier.ecommercecore.common.messaging.MessageCorrelationAdapter;
import com.alier.ecommerceproductservice.domain.event.ProductCreatedEvent;
import com.alier.ecommerceproductservice.domain.event.ProductStatusChangedEvent;
import com.alier.ecommerceproductservice.domain.event.ProductUpdatedEvent;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for publishing product domain events to Kafka topics.
 * This implementation preserves trace and correlation IDs across service boundaries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${kafka.topic.product-created}")
    private String productCreatedTopic;
    @Value("${kafka.topic.product-updated}")
    private String productUpdatedTopic;

    /**
     * Publishes a ProductCreatedEvent to Kafka.
     *
     * @param event the product created event
     */
    public void publishProductCreated(ProductCreatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getId().toString();

            log.info("Publishing ProductCreatedEvent to topic {} with key {}", productCreatedTopic, key);

            // Use the correlation adapter to prepare outgoing message headers
            sendEventWithCorrelationHeaders(productCreatedTopic, key, eventJson, "ProductCreatedEvent");
        } catch (JsonProcessingException e) {
            log.error("Error serializing ProductCreatedEvent to JSON: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error publishing ProductCreatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Creates and publishes a ProductCreatedEvent from a Product domain model.
     *
     * @param product the product that was created
     */
    public void publishProductCreatedEvent(Product product) {
        ProductCreatedEvent event = ProductCreatedEvent.fromProduct(product);
        publishProductCreated(event);
    }

    /**
     * Publishes a ProductUpdatedEvent to Kafka.
     *
     * @param event the product updated event
     */
    public void publishProductUpdated(ProductUpdatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getId().toString();

            log.info("Publishing ProductUpdatedEvent to topic {} with key {}", productUpdatedTopic, key);

            // Use the correlation adapter to prepare outgoing message headers
            sendEventWithCorrelationHeaders(productUpdatedTopic, key, eventJson, "ProductUpdatedEvent");
        } catch (JsonProcessingException e) {
            log.error("Error serializing ProductUpdatedEvent to JSON: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error publishing ProductUpdatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Creates and publishes a ProductUpdatedEvent from a Product domain model.
     *
     * @param product the product that was updated
     */
    public void publishProductUpdatedEvent(Product product) {
        ProductUpdatedEvent event = ProductUpdatedEvent.fromProduct(product);
        publishProductUpdated(event);
    }

    /**
     * Creates and publishes a ProductStatusChangedEvent from a Product domain model.
     *
     * @param product          the product whose status was changed
     * @param statusChangeType the type of status change (e.g., "ACTIVATED", "DEACTIVATED")
     */
    public void publishProductStatusChangedEvent(Product product, String statusChangeType) {
        ProductStatusChangedEvent event = new ProductStatusChangedEvent(
                product.getId(),
                product.getSku(),
                product.getStatus(),
                statusChangeType,
                LocalDateTime.now()
        );

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = product.getId().toString();

            log.info("Publishing ProductStatusChangedEvent to topic {} with key {}", productUpdatedTopic, key);

            // Use the correlation adapter to prepare outgoing message headers
            sendEventWithCorrelationHeaders(productUpdatedTopic, key, eventJson, "ProductStatusChangedEvent");
        } catch (JsonProcessingException e) {
            log.error("Error serializing ProductStatusChangedEvent to JSON: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error publishing ProductStatusChangedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper method to send an event with correlation headers.
     * This ensures correlation IDs are propagated to downstream services.
     *
     * @param topic     The Kafka topic to publish to
     * @param key       The message key
     * @param payload   The JSON payload
     * @param eventType The type of event (for logging)
     */
    private void sendEventWithCorrelationHeaders(String topic, String key, String payload, String eventType) {
        // Create a message builder with payload and key
        MessageBuilder<String> messageBuilder = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key);

        // Use our MessageCorrelationAdapter to add trace and correlation headers
        MessageCorrelationAdapter.prepareOutgoingMessageHeaders(messageBuilder::setHeader);

        // Build and send the message
        Message<String> message = messageBuilder.build();
        kafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("{} sent successfully to topic: {}, partition: {}, offset: {}, traceId: {}",
                                eventType,
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                CorrelationContext.getTraceId());
                    } else {
                        log.error("Failed to send {} to Kafka: {}, traceId: {}",
                                eventType, ex.getMessage(), CorrelationContext.getTraceId(), ex);
                    }
                });
    }
} 