package com.personal.store_api.service;

import com.personal.store_api.document.ProductDocument;
import com.personal.store_api.entity.Product;
import com.personal.store_api.entity.ProductVariant;
import com.personal.store_api.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchService {

    private final ProductSearchRepository productSearchRepository;

    public void indexProduct(Product product) {
        try {
            ProductDocument document = convertToDocument(product);
            if (document != null) {
                productSearchRepository.save(document);
                log.info("Indexed product to Elasticsearch: {}", product.getName());
            }
        } catch (Exception e) {
            log.error("Failed to index product {}: {}", product.getId(), e.getMessage());
        }
    }

    public void updateProduct(Product product) {
        try {
            deleteProduct(product.getId());
            indexProduct(product);
            log.info("Updated product in Elasticsearch: {}", product.getId());
        } catch (Exception e) {
            log.error("Failed to update product {}: {}", product.getId(), e.getMessage());
        }
    }

    public void deleteProduct(Integer productId) {
        try {
            productSearchRepository.deleteById(String.valueOf(productId));
            log.info("Deleted product from Elasticsearch: {}", productId);
        } catch (Exception e) {
            log.error("Failed to delete product {}: {}", productId, e.getMessage());
        }
    }

    public Page<ProductDocument> searchProducts(
            String query,
            Double minPrice,
            Double maxPrice,
            String brandName,
            String categoryName,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Pageable pageable = PageRequest.of(page, size, createSort(sortBy, sortDirection));

        // If query is empty and no filters, return all products
        if ((query == null || query.trim().isEmpty()) && 
            (brandName == null || brandName.isEmpty()) && 
            (categoryName == null || categoryName.isEmpty())) {
            return productSearchRepository.findAll(pageable);
        }

        // Use search query when there's a query or filters
        if (brandName != null && !brandName.isEmpty() && categoryName != null && !categoryName.isEmpty()) {
            return productSearchRepository.searchProducts(query, minPrice, maxPrice, brandName, categoryName, pageable);
        } else if (brandName != null && !brandName.isEmpty()) {
            return productSearchRepository.searchProducts(query, minPrice, maxPrice, brandName, pageable);
        } else if (categoryName != null && !categoryName.isEmpty()) {
            return productSearchRepository.searchProductsByCategory(query, minPrice, maxPrice, categoryName, pageable);
        } else {
            return productSearchRepository.searchProducts(query, minPrice, maxPrice, pageable);
        }
    }

    public List<ProductDocument> suggestProducts(String prefix) {
        try {
            return productSearchRepository.findByNameStartingWithIgnoreCase(prefix);
        } catch (Exception e) {
            log.error("Failed to get suggestions for prefix {}: {}", prefix, e.getMessage());
            return List.of();
        }
    }

    private ProductDocument convertToDocument(Product product) {
        if (product == null) {
            return null;
        }

        ProductVariant firstVariant = product.getVariants() != null && !product.getVariants().isEmpty()
                ? product.getVariants().get(0)
                : null;

        Double price = firstVariant != null && firstVariant.getPrice() != null
                ? firstVariant.getPrice().doubleValue()
                : 0.0;
        String image = firstVariant != null && firstVariant.getImage() != null
                ? firstVariant.getImage()
                : product.getImage();

        return ProductDocument.builder()
                .id(String.valueOf(product.getId()))
                .productId(product.getId())
                .name(product.getName())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .brandName(product.getBrand() != null ? product.getBrand().getName() : "")
                .price(price)
                .image(image)
                .build();
    }

    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "price";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(direction, sortBy);
    }
}
