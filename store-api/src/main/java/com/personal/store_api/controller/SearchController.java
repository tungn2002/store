package com.personal.store_api.controller;

import com.personal.store_api.document.ProductDocument;
import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.entity.Product;
import com.personal.store_api.repository.ProductRepository;
import com.personal.store_api.service.ElasticsearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {

    ElasticsearchService elasticsearchService;
    ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDocument>>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "25") int size,
            @RequestParam(required = false, defaultValue = "price") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {

        Double actualMinPrice = minPrice != null ? minPrice : 0.0;
        Double actualMaxPrice = maxPrice != null ? maxPrice : 999999999.0;

        Page<ProductDocument> result = elasticsearchService.searchProducts(
                query,
                actualMinPrice,
                actualMaxPrice,
                brand,
                category,
                page,
                size,
                sortBy,
                sortDirection
        );

        ApiResponse<Page<ProductDocument>> apiResponse = ApiResponse.<Page<ProductDocument>>builder()
                .result(result)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> suggestProducts(
            @RequestParam String prefix) {

        List<ProductDocument> suggestions = elasticsearchService.suggestProducts(prefix);

        ApiResponse<List<ProductDocument>> apiResponse = ApiResponse.<List<ProductDocument>>builder()
                .result(suggestions)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<String>> reindexAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            for (Product product : products) {
                elasticsearchService.indexProduct(product);
            }
            
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .result("Reindexed " + products.size() + " products")
                    .build();

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .result("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }
}
