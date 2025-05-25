package com.alier.ecommerceproductservice.infrastructure.controller;

import com.alier.ecommercecore.common.dto.PaginatedResponse;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.exception.ConflictException;
import com.alier.ecommerceproductservice.application.dto.CreateProductRequest;
import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import com.alier.ecommerceproductservice.application.dto.UpdateProductRequest;
import com.alier.ecommerceproductservice.application.usecase.CreateProductUseCase;
import com.alier.ecommerceproductservice.application.usecase.GetProductUseCase;
import com.alier.ecommerceproductservice.application.usecase.UpdateProductUseCase;
import com.alier.ecommerceproductservice.domain.exception.ProductErrorCode;
import com.alier.ecommerceproductservice.domain.model.ProductStatus;
import com.alier.ecommerceproductservice.domain.repository.ProductRepository;
import com.alier.ecommerceproductservice.infrastructure.repository.ProductRepositoryAdapter;
import com.alier.ecommerceproductservice.infrastructure.search.ProductSearchService;
import com.alier.ecommercewebcore.rest.exception.GlobalRestExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductController.class)
@Import({ProductRepositoryAdapter.class, ProductController.class, GlobalRestExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CreateProductUseCase createProductUseCase;
    @MockitoBean
    private GetProductUseCase getProductUseCase;
    @MockitoBean
    private UpdateProductUseCase updateProductUseCase;
    @MockitoBean
    private ProductSearchService searchService;
    @MockitoBean
    private ProductRepository productRepository; // ✅ Needed for controller injection

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Given
        CreateProductRequest request = new CreateProductRequest(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "TEST-SKU-123",
                List.of("test")
        );

        ProductDTO responseDTO = ProductDTO.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .sku("TEST-SKU-123")
                .status(ProductStatus.DRAFT)
                .build();

        when(createProductUseCase.execute(any())).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.sku").value("TEST-SKU-123"));
    }

    @Test
    @DisplayName("Should return 409 CONFLICT when creating product with duplicate SKU")
    void shouldReturnConflictWhenCreateProductWithDuplicateSku() throws Exception {
        // Given
        CreateProductRequest request = new CreateProductRequest(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "DUPLICATE-SKU",
                List.of("test")
        );

        when(createProductUseCase.execute(any(CreateProductRequest.class)))
                .thenThrow(BusinessException.conflict(ProductErrorCode.PRODUCT_SKU_EXISTS));

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(ProductErrorCode.PRODUCT_SKU_EXISTS.getDefaultMessage())));
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST when creating product with invalid data")
    void shouldReturnBadRequestWhenCreateProductWithInvalidData() throws Exception {
        // Given
        CreateProductRequest request = new CreateProductRequest(
                "Very Long Product Name That Exceeds The Maximum Length Allowed",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "TEST-SKU-123",
                List.of("test")
        );

        when(createProductUseCase.execute(any(CreateProductRequest.class)))
                .thenThrow(BusinessException.validation(ProductErrorCode.PRODUCT_NAME_TOO_LONG));

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(ProductErrorCode.PRODUCT_NAME_TOO_LONG.getDefaultMessage())));
    }

    @Test
    @DisplayName("Should retrieve product by ID")
    void shouldRetrieveProductById() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        ProductDTO responseDTO = ProductDTO.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .sku("TEST-SKU-123")
                .status(ProductStatus.ACTIVE)
                .build();

        when(getProductUseCase.getById(productId)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(productId.toString())))
                .andExpect(jsonPath("$.data.name", is("Test Product")));
    }

    @Test
    @DisplayName("Should return 404 NOT FOUND when product does not exist")
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();

        when(getProductUseCase.getById(productId))
                .thenThrow(BusinessException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(ProductErrorCode.PRODUCT_NOT_FOUND.getDefaultMessage())));
    }

    @Test
    @DisplayName("Should retrieve all products")
    void shouldRetrieveAllProducts() throws Exception {
        // Given
        ProductDTO product1 = ProductDTO.builder()
                .id(UUID.randomUUID())
                .name("Product 1")
                .sku("SKU-1")
                .build();

        ProductDTO product2 = ProductDTO.builder()
                .id(UUID.randomUUID())
                .name("Product 2")
                .sku("SKU-2")
                .build();

        List<ProductDTO> products = Arrays.asList(product1, product2);
        PaginatedResponse<ProductDTO> paginatedResponse = PaginatedResponse.of(products, 0, 20, 2);

        when(getProductUseCase.getAllProductsPaged(0, 20, "id", "asc")).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].name", is("Product 1")))
                .andExpect(jsonPath("$.data.content[1].name", is("Product 2")))
                .andExpect(jsonPath("$.data.totalElements", is(2)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(20)))
                .andExpect(jsonPath("$.data.totalPages", is(1)));
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Product",
                "Updated Description",
                new BigDecimal("149.99"),
                20
        );

        ProductDTO responseDTO = ProductDTO.builder()
                .id(productId)
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(20)
                .imageUrls(List.of("http://example.com/image.jpg"))
                .status(ProductStatus.ACTIVE)
                .build();

        when(updateProductUseCase.execute(Mockito.eq(productId), any(UpdateProductRequest.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product updated successfully")))
                .andExpect(jsonPath("$.data.name", is("Updated Product")))
                .andExpect(jsonPath("$.data.price", is(149.99)));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent product")
    void shouldReturnNotFoundWhenUpdatingNonExistentProduct() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Product",
                "Updated Description",
                new BigDecimal("149.99"),
                20
        );

        when(updateProductUseCase.execute(Mockito.eq(productId), any(UpdateProductRequest.class)))
                .thenThrow(BusinessException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND));

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(ProductErrorCode.PRODUCT_NOT_FOUND.getDefaultMessage())));
    }

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
} 