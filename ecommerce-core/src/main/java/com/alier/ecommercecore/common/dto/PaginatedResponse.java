package com.alier.ecommercecore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic response object for paginated data across the e-commerce platform.
 *
 * @param <T> The type of items in the page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PaginatedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        return PaginatedResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(calculateTotalPages(size, totalElements))
                .build();
    }

    private static int calculateTotalPages(int size, long totalElements) {
        return size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }
} 