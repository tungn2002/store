package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.ProductVariantRequest;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.ProductVariantResponse;
import com.personal.store_api.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/products/{productId}/variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductVariantController {

    ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductVariantResponse>>> getProductVariants(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        PaginatedResponse<ProductVariantResponse> response = productVariantService.getProductVariants(productId, page, size, sortBy);

        ApiResponse<PaginatedResponse<ProductVariantResponse>> apiResponse = ApiResponse.<PaginatedResponse<ProductVariantResponse>>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getProductVariant(
            @PathVariable Integer productId,
            @PathVariable Integer id) {
        ProductVariantResponse response = productVariantService.getProductVariant(id);

        ApiResponse<ProductVariantResponse> apiResponse = ApiResponse.<ProductVariantResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createProductVariant(
            @PathVariable Integer productId,
            @ModelAttribute @Valid ProductVariantRequest request) throws IOException {
        ProductVariantResponse response = productVariantService.createProductVariant(request);

        ApiResponse<ProductVariantResponse> apiResponse = ApiResponse.<ProductVariantResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateProductVariant(
            @PathVariable Integer productId,
            @PathVariable Integer id,
            @ModelAttribute @Valid ProductVariantRequest request) throws IOException {
        ProductVariantResponse response = productVariantService.updateProductVariant(id, request);

        ApiResponse<ProductVariantResponse> apiResponse = ApiResponse.<ProductVariantResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductVariant(
            @PathVariable Integer productId,
            @PathVariable Integer id) throws IOException {
        productVariantService.deleteProductVariant(id);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .result(null)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
