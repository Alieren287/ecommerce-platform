package com.alier.ecommercecore.common.messaging;

import com.alier.ecommercecore.common.logging.CorrelationIds;
import lombok.extern.slf4j.Slf4j;

/**
 * Example showing how to use the MessageCorrelationAdapter with Kafka.
 * This is for demonstration purposes only - you'd implement this in your actual Kafka consumer/producer classes.
 */
@Slf4j
public class KafkaCorrelationExample {

    /**
     * Example of how to use MessageCorrelationAdapter in a Kafka consumer.
     *
     * @param record The Kafka record (simplified for the example)
     */
    public void processOrderEvent(Object record) {
        // In a real Kafka consumer, you would extract headers from the record
        // This is a simplified example
        MessageCorrelationAdapter.processMessageWithCorrelation(
                // Extract header function
                headerName -> {
                    // In a real implementation, you'd get this from the Kafka record headers
                    if (CorrelationIds.TRACE_ID_HEADER.equals(headerName)) {
                        return "existing-trace-id-from-kafka";
                    }
                    return null;
                },
                // Message processing function
                message -> {
                    // Your actual message processing logic here
                    log.info("Processing order event");
                    // Call other services, which will automatically inherit the correlation IDs
                    return "processed result";
                },
                record
        );
    }

    /**
     * Example of how to use MessageCorrelationAdapter when producing a Kafka message.
     *
     * @param producer The Kafka producer (simplified for the example)
     * @param payload  The message payload
     */
    public void sendOrderEvent(Object producer, Object payload) {
        // In a real implementation, you'd set headers on your ProducerRecord
        MessageCorrelationAdapter.prepareOutgoingMessageHeaders(
                // Header setter function
                (headerName, headerValue) -> {
                    // In a real implementation, you'd set this on the Kafka ProducerRecord headers
                    log.info("Setting Kafka header: {} = {}", headerName, headerValue);
                }
        );

        // Then send your message with the headers
        log.info("Sending order event");
    }
} 