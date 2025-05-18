package com.alier.ecommerceproductservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Main application test class to verify that the application context loads correctly.
 * All bean configuration and mocks are handled in {@link TestConfig}.
 */
@SpringBootTest(
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "spring.kafka.listener.missing-topics-fatal=false",
                "spring.data.redis.repositories.enabled=false",
                "spring.data.elasticsearch.repositories.enabled=false",
                "spring.elasticsearch.repositories.enabled=false",
                "spring.cache.type=none",
                "spring.elasticsearch.connection-timeout=1ms",
                "spring.elasticsearch.socket-timeout=1ms",
                "spring.elasticsearch.rest.connection-timeout=1ms",
                "spring.elasticsearch.rest.read-timeout=1ms",
                "spring.main.allow-circular-references=true",
                "spring.elasticsearch.enabled=false"
        }
)
@EnableAutoConfiguration(exclude = {
        RedisAutoConfiguration.class,
        KafkaAutoConfiguration.class,
        ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRepositoriesAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class
})
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class EcommerceProductServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test passes if application context loads successfully
    }
}
