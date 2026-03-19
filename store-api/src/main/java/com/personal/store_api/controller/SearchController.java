package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.response.ProductResponse;
import com.personal.store_api.service.SearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {

    SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
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

        Page<ProductResponse> result = searchService.searchProducts(
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

        ApiResponse<Page<ProductResponse>> apiResponse = ApiResponse.<Page<ProductResponse>>builder()
                .result(result)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> suggestProducts(
            @RequestParam String prefix,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        Page<ProductResponse> suggestions = searchService.suggestProducts(prefix, limit);

        ApiResponse<Page<ProductResponse>> apiResponse = ApiResponse.<Page<ProductResponse>>builder()
                .result(suggestions)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
