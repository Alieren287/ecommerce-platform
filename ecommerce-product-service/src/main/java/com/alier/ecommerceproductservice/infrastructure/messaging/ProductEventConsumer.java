package com.alier.ecommerceproductservice.infrastructure.messaging;

import com.alier.ecommercecore.common.logging.CorrelationContext;
import com.alier.ecommercecore.common.messaging.MessageCorrelationAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Example Kafka consumer that demonstrates how to use the correlation context.
 * This class is marked with @Profile("demo") to prevent it from running in production.
 */
@Component
@Profile("demo")
@Slf4j
@RequiredArgsConstructor
public class ProductEventConsumer {

    /**
     * Example listener method that processes product inventory events from another service.
     * Shows how to extract correlation IDs from Kafka headers.
     *
     * @param record the Kafka record
     */
    @KafkaListener(topics = "inventory-updated", groupId = "${kafka.consumer.group-id}")
    public void processInventoryUpdatedEvent(ConsumerRecord<String, String> record) {
        // Use the MessageCorrelationAdapter to process the message with correlation IDs
        MessageCorrelationAdapter.processMessageWithCorrelation(
                // Header extraction function
                headerName -> extractHeader(record, headerName),

                // Actual message processing logic
                payload -> {
                    try {
                        log.info("Received inventory update event with key: {}, traceId: {}",
                                record.key(), CorrelationContext.getTraceId());

                        // Process the message
                        // In a real implementation, you would:
                        // 1. Deserialize the message to your domain event type
                        // 2. Process the event in your domain service
                        // 3. Update relevant domain state

                        log.info("Successfully processed inventory update event, traceId: {}",
                                CorrelationContext.getTraceId());
                        return null;
                    } catch (Exception e) {
                        log.error("Error processing inventory event: {}, traceId: {}",
                                e.getMessage(), CorrelationContext.getTraceId(), e);
                        throw e;
                    }
                },
                record.value()
        );
    }

    /**
     * Extracts a header value from a Kafka record.
     *
     * @param record     the Kafka record
     * @param headerName the name of the header to extract
     * @return the header value, or null if not found
     */
    private String extractHeader(ConsumerRecord<String, String> record, String headerName) {
        return Optional.ofNullable(record.headers().lastHeader(headerName))
                .map(Header::value)
                .map(value -> new String(value, StandardCharsets.UTF_8))
                .orElse(null);
    }
} 