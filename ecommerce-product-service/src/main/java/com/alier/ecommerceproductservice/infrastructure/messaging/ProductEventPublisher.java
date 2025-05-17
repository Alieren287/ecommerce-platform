package com.alier.ecommerceproductservice.infrastructure.messaging;

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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for publishing product domain events to Kafka topics.
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
            kafkaTemplate.send(productCreatedTopic, key, eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("ProductCreatedEvent sent successfully to topic: {}, partition: {}, offset: {}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to send ProductCreatedEvent to Kafka: {}", ex.getMessage(), ex);
                        }
                    });
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
            kafkaTemplate.send(productUpdatedTopic, key, eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("ProductUpdatedEvent sent successfully to topic: {}, partition: {}, offset: {}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to send ProductUpdatedEvent to Kafka: {}", ex.getMessage(), ex);
                        }
                    });
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
            kafkaTemplate.send(productUpdatedTopic, key, eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("ProductStatusChangedEvent sent successfully");
                        } else {
                            log.error("Failed to send ProductStatusChangedEvent to Kafka: {}", ex.getMessage(), ex);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Error serializing ProductStatusChangedEvent to JSON: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error publishing ProductStatusChangedEvent: {}", e.getMessage(), e);
        }
    }
} 