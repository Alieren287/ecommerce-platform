package com.alier.ecommerceproductservice.infrastructure.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Configuration for Elasticsearch.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.alier.ecommerceproductservice.infrastructure.search.repository")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:localhost:9200}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Bean
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl);

        if (!username.isEmpty() && !password.isEmpty()) {
            return builder.withBasicAuth(username, password).build();
        }

        return builder.build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ClientConfiguration clientConfiguration) {
        return ElasticsearchClients.createImperative(clientConfiguration);
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
        ElasticsearchConverter converter = new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
        return new ElasticsearchTemplate(elasticsearchClient, converter);
    }
} 