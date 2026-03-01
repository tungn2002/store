package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.BrandRequest;
import com.personal.store_api.dto.response.BrandResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.service.BrandService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandController {

    BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<BrandResponse>>> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        PaginatedResponse<BrandResponse> response = brandService.getBrands(page, size, sortBy);

        ApiResponse<PaginatedResponse<BrandResponse>> apiResponse = ApiResponse.<PaginatedResponse<BrandResponse>>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @RequestBody @Valid BrandRequest request) {
        BrandResponse response = brandService.createBrand(request);

        ApiResponse<BrandResponse> apiResponse = ApiResponse.<BrandResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable Integer id,
            @RequestBody @Valid BrandRequest request) {
        BrandResponse response = brandService.updateBrand(id, request);

        ApiResponse<BrandResponse> apiResponse = ApiResponse.<BrandResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .result(null)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
