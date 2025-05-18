package com.alier.ecommerceproductservice;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.alier.ecommerceproductservice.infrastructure.cache.ProductCacheService;
import com.alier.ecommerceproductservice.infrastructure.config.ElasticsearchConfig;
import com.alier.ecommerceproductservice.infrastructure.messaging.ProductEventPublisher;
import com.alier.ecommerceproductservice.infrastructure.search.ProductSearchService;
import com.alier.ecommerceproductservice.infrastructure.search.repository.ProductElasticsearchRepository;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;

/**
 * Consolidated test configuration that provides mock beans for all external services
 * to allow application context to load during tests.
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = {
        ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRepositoriesAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class
})
// This will override the @EnableElasticsearchRepositories in ElasticsearchConfig
@EnableElasticsearchRepositories(considerNestedRepositories = true)
public class TestConfig {

    // ========== Main Config Mocks ==========

    // Mock the main application's config classes to prevent them from creating beans
    @MockBean
    private ElasticsearchConfig elasticsearchConfig;

    // Mock the Elasticsearch repository
    @MockBean
    private ProductElasticsearchRepository productElasticsearchRepository;

    @Bean
    @Primary
    @ConditionalOnMissingBean(ProductElasticsearchRepository.class)
    public ProductElasticsearchRepository productElasticsearchRepository() {
        return mock(ProductElasticsearchRepository.class);
    }

    // ========== Kafka Mocks ==========

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    @Bean
    @Primary
    public ProductEventPublisher productEventPublisher() {
        return mock(ProductEventPublisher.class);
    }

    // ========== Redis Configuration ==========

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        // Create a dummy connection factory that doesn't actually try to connect
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        // Don't validate connection during initialization
        factory.setValidateConnection(false);
        // Skip actual connect attempts
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        // Use a simple in-memory cache manager for tests
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public ProductCacheService productCacheService() {
        return mock(ProductCacheService.class);
    }

    // ========== Elasticsearch Configuration ==========

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .withConnectTimeout(1)
                .withSocketTimeout(1)
                .build();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RestClient restClient() {
        return mock(RestClient.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ElasticsearchClient elasticsearchClient() {
        return mock(ElasticsearchClient.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ElasticsearchOperations elasticsearchOperations() {
        return mock(ElasticsearchOperations.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ElasticsearchTemplate elasticsearchTemplate() {
        return mock(ElasticsearchTemplate.class);
    }

    @Bean
    @Primary
    public ProductSearchService productSearchService() {
        return mock(ProductSearchService.class);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ElasticsearchConverter elasticsearchConverter() {
        return new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public JacksonJsonpMapper jacksonJsonpMapper() {
        return new JacksonJsonpMapper();
    }
} 