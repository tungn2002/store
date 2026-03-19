package com.personal.store_api.service;

import com.personal.store_api.dto.response.ProductResponse;
import com.personal.store_api.entity.Product;
import com.personal.store_api.mapper.ProductMapper;
import com.personal.store_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    final ProductRepository productRepository;
    final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String query,
            Double minPrice,
            Double maxPrice,
            String brandName,
            String categoryName,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        Page<Product> productPage;

        // Handle different filter combinations
        if (hasText(query) && hasText(brandName) && hasText(categoryName)) {
            productPage = productRepository.searchWithFilters(
                    query, brandName, categoryName, minPrice, maxPrice, pageable);
        } else if (hasText(brandName) && hasText(categoryName)) {
            productPage = productRepository.searchWithFilters(
                    "", brandName, categoryName, minPrice, maxPrice, pageable);
        } else if (hasText(query) && hasText(brandName)) {
            productPage = productRepository.searchWithFilters(
                    query, brandName, null, minPrice, maxPrice, pageable);
        } else if (hasText(query) && hasText(categoryName)) {
            productPage = productRepository.searchWithFilters(
                    query, null, categoryName, minPrice, maxPrice, pageable);
        } else if (hasText(brandName)) {
            productPage = productRepository.searchWithFilters(
                    query, brandName, null, minPrice, maxPrice, pageable);
        } else if (hasText(categoryName)) {
            productPage = productRepository.searchWithFilters(
                    query, null, categoryName, minPrice, maxPrice, pageable);
        } else if (hasText(query)) {
            productPage = productRepository.searchWithFilters(
                    query, null, null, minPrice, maxPrice, pageable);
        } else {
            // No filters - return all products with price range
            productPage = productRepository.findAllWithPriceRange(minPrice, maxPrice, pageable);
        }

        return productPage.map(productMapper::toProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> suggestProducts(String prefix, int limit) {
        if (!hasText(prefix)) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(0, limit);
        Page<Product> productPage = productRepository.findByNameStartingWithIgnoreCase(prefix, pageable);

        return productPage.map(productMapper::toProductResponse);
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "price";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
