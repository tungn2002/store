package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.BrandRequest;
import com.personal.store_api.dto.response.BrandResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for brand management operations.
 */
@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * Get paginated list of brands.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('brands.read')")
    public ResponseEntity<ApiResponse<PaginatedResponse<BrandResponse>>> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        PaginatedResponse<BrandResponse> response = brandService.getBrands(page, size, sortBy);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Get all brands (no pagination).
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        List<BrandResponse> response = brandService.getAllBrands();
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Create a new brand.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('brands.create')")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @RequestBody @Valid BrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Update an existing brand.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('brands.update')")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable Integer id,
            @RequestBody @Valid BrandRequest request) {
        BrandResponse response = brandService.updateBrand(id, request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Delete a brand by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('brands.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(buildResponse());
    }

    /**
     * Build success response with result.
     */
    private <T> ApiResponse<T> buildResponse(T result) {
        return ApiResponse.<T>builder()
                .result(result)
                .build();
    }

    /**
     * Build success response without result.
     */
    private ApiResponse<Void> buildResponse() {
        return ApiResponse.<Void>builder()
                .build();
    }
}
