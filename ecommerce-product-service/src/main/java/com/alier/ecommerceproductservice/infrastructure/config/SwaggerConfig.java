package com.alier.ecommerceproductservice.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.UUID;

/**
 * Swagger/OpenAPI configuration.
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization"))
                        .addSchemas("UUID", new Schema<UUID>()
                                .type("string")
                                .format("uuid")
                                .example("123e4567-e89b-12d3-a456-426614174000")
                                .description("UUID format string")))
                .info(new Info()
                        .title(applicationName + " API")
                        .description("RESTful API for managing products in the ecommerce platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("Private")));
    }

    @Bean
    public OpenApiCustomizer uuidSchemaCustomizer() {
        return openApi -> {
            // Add UUID format to all string schemas with format "uuid"
            for (Map.Entry<String, Schema> entry : openApi.getComponents().getSchemas().entrySet()) {
                if (entry.getValue().getProperties() != null) {
                    for (Object propertyObj : entry.getValue().getProperties().values()) {
                        Schema property = (Schema) propertyObj;
                        if (property instanceof StringSchema && "uuid".equals(property.getFormat())) {
                            property.setExample(UUID.randomUUID().toString());
                        }
                    }
                }
            }
        };
    }
} 