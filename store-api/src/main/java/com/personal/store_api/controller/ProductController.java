package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.ProductRequest;
import com.personal.store_api.dto.response.NewProductResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.ProductDetailResponse;
import com.personal.store_api.dto.response.ProductResponse;
import com.personal.store_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controller for product management operations.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Get paginated list of products with optional filters.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('products.read')")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId) {
        PaginatedResponse<ProductResponse> response = productService.getProducts(page, size, sortBy, name, categoryId, brandId);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Get latest 5 products.
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<NewProductResponse>>> getLatest5Products() {
        List<NewProductResponse> products = productService.getLatest5Products();
        return ResponseEntity.ok(buildResponse(products));
    }

    /**
     * Get product by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('products.read_one')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Integer id) {
        ProductResponse response = productService.getProduct(id);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Get product detail by ID.
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable Integer id) {
        ProductDetailResponse response = productService.getProductDetail(id);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Create a new product.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('products.create')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @ModelAttribute @Valid ProductRequest request) throws IOException {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Update an existing product.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('products.update')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Integer id,
            @ModelAttribute @Valid ProductRequest request) throws IOException {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Delete a product by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('products.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Integer id) throws IOException {
        productService.deleteProduct(id);
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
