package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.ProductVariantRequest;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.ProductVariantResponse;
import com.personal.store_api.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller for product variant management operations.
 */
@RestController
@RequestMapping("/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    /**
     * Get paginated list of variants for a product.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('variants.read')")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductVariantResponse>>> getProductVariants(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        PaginatedResponse<ProductVariantResponse> response = productVariantService.getProductVariants(productId, page, size, sortBy);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Get variant by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('variants.read_one')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getProductVariant(
            @PathVariable Integer productId,
            @PathVariable Integer id) {
        ProductVariantResponse response = productVariantService.getProductVariant(id);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Create a new variant for a product.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('variants.create')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createProductVariant(
            @PathVariable Integer productId,
            @ModelAttribute @Valid ProductVariantRequest request) throws IOException {
        ProductVariantResponse response = productVariantService.createProductVariant(request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Update an existing variant.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('variants.update')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateProductVariant(
            @PathVariable Integer productId,
            @PathVariable Integer id,
            @ModelAttribute @Valid ProductVariantRequest request) throws IOException {
        ProductVariantResponse response = productVariantService.updateProductVariant(id, request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Delete a variant by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('variants.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteProductVariant(
            @PathVariable Integer productId,
            @PathVariable Integer id) throws IOException {
        productVariantService.deleteProductVariant(id);
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
