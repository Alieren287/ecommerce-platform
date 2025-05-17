package com.alier.ecommerceproductservice.infrastructure.search;

import com.alier.ecommercecore.common.dto.PaginatedResponse;
import com.alier.ecommerceproductservice.application.dto.ProductDTO;
import com.alier.ecommerceproductservice.application.dto.ProductFilterRequest;
import com.alier.ecommerceproductservice.domain.model.Product;
import com.alier.ecommerceproductservice.infrastructure.search.document.ProductDocument;
import com.alier.ecommerceproductservice.infrastructure.search.repository.ProductElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for product search using Elasticsearch.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ProductElasticsearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Index a product in Elasticsearch
     *
     * @param product the product to index
     */
    public void indexProduct(Product product) {
        log.debug("Indexing product in Elasticsearch: {}", product.getId());
        ProductDocument document = ProductDocument.fromDomain(product);
        repository.save(document);
    }

    /**
     * Index multiple products in Elasticsearch
     *
     * @param products the products to index
     */
    public void indexProducts(List<Product> products) {
        log.debug("Indexing {} products in Elasticsearch", products.size());
        List<ProductDocument> documents = products.stream()
                .map(ProductDocument::fromDomain)
                .collect(Collectors.toList());
        repository.saveAll(documents);
    }

    /**
     * Delete a product from the Elasticsearch index
     *
     * @param productId the ID of the product to delete
     */
    public void deleteProduct(String productId) {
        log.debug("Deleting product from Elasticsearch: {}", productId);
        repository.deleteById(productId);
    }

    /**
     * Search products with advanced text search capabilities
     *
     * @param query   the search query
     * @param page    the page number
     * @param size    the page size
     * @param sortBy  the field to sort by
     * @param sortDir the sort direction
     * @return paginated search results
     */
    public PaginatedResponse<ProductDTO> searchProducts(String query, int page, int size, String sortBy, String sortDir) {
        log.debug("Searching products with query: {}", query);

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductDocument> searchResults = repository.search(query, pageable);

        List<ProductDTO> products = searchResults.getContent().stream()
                .map(ProductDocument::toDomain)
                .map(ProductDTO::fromDomain)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                products,
                page,
                size,
                searchResults.getTotalElements()
        );
    }

    /**
     * Filter products with complex criteria
     *
     * @param filter  the filter criteria
     * @param page    the page number
     * @param size    the page size
     * @param sortBy  the field to sort by
     * @param sortDir the sort direction
     * @return paginated filtered results
     */
    public PaginatedResponse<ProductDTO> filterProducts(ProductFilterRequest filter, int page, int size, String sortBy, String sortDir) {
        log.debug("Filtering products with criteria: {}", filter);

        Criteria criteria = new Criteria();

        // Add criteria based on filter
        if (filter.getName() != null && !filter.getName().isEmpty()) {
            criteria = criteria.and("name").contains(filter.getName());
        }

        if (filter.getSku() != null && !filter.getSku().isEmpty()) {
            criteria = criteria.and("sku").contains(filter.getSku());
        }

        if (filter.getStatus() != null) {
            criteria = criteria.and("status").is(filter.getStatus());
        }

        if (filter.getMinPrice() != null && filter.getMaxPrice() != null) {
            criteria = criteria.and("price").between(filter.getMinPrice(), filter.getMaxPrice());
        } else if (filter.getMinPrice() != null) {
            criteria = criteria.and("price").greaterThanEqual(filter.getMinPrice());
        } else if (filter.getMaxPrice() != null) {
            criteria = criteria.and("price").lessThanEqual(filter.getMaxPrice());
        }

        if (filter.getMinStock() != null && filter.getMaxStock() != null) {
            criteria = criteria.and("stockQuantity").between(filter.getMinStock(), filter.getMaxStock());
        } else if (filter.getMinStock() != null) {
            criteria = criteria.and("stockQuantity").greaterThanEqual(filter.getMinStock());
        } else if (filter.getMaxStock() != null) {
            criteria = criteria.and("stockQuantity").lessThanEqual(filter.getMaxStock());
        }

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        return getPaginatedResponseFromElasticSearch(page, size, query);
    }

    /**
     * Execute a raw Elasticsearch query
     *
     * @param queryString the JSON query string
     * @param page        the page number
     * @param size        the page size
     * @return paginated results
     */
    public PaginatedResponse<ProductDTO> executeRawQuery(String queryString, int page, int size) {
        log.debug("Executing raw Elasticsearch query: {}", queryString);

        Pageable pageable = PageRequest.of(page, size);
        Query query = new StringQuery(queryString).setPageable(pageable);

        return getPaginatedResponseFromElasticSearch(page, size, query);
    }

    private PaginatedResponse<ProductDTO> getPaginatedResponseFromElasticSearch(int page, int size, Query query) {
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

        List<ProductDTO> products = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(ProductDocument::toDomain)
                .map(ProductDTO::fromDomain)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                products,
                page,
                size,
                searchHits.getTotalHits()
        );
    }

    /**
     * Reindex all products from the repository
     *
     * @param products the products to reindex
     */
    public void reindexAll(List<Product> products) {
        log.info("Reindexing all products in Elasticsearch");
        repository.deleteAll();
        indexProducts(products);
    }

    /**
     * Create a Sort object based on the given field and direction
     */
    private Sort createSort(String sortBy, String sortDir) {
        // Set default sort field if empty or null
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "id";
        }

        // Set direction
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }
} 