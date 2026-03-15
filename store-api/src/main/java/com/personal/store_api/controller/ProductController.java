package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.ProductRequest;
import com.personal.store_api.dto.response.NewProductResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.ProductDetailResponse;
import com.personal.store_api.dto.response.ProductResponse;
import com.personal.store_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductService productService;

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

        ApiResponse<PaginatedResponse<ProductResponse>> apiResponse = ApiResponse.<PaginatedResponse<ProductResponse>>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<NewProductResponse>>> getLatest5Products() {
        List<NewProductResponse> products = productService.getLatest5Products();

        ApiResponse<List<NewProductResponse>> apiResponse = ApiResponse.<List<NewProductResponse>>builder()
                .result(products)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('products.read_one')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Integer id) {
        ProductResponse response = productService.getProduct(id);

        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable Integer id) {
        ProductDetailResponse response = productService.getProductDetail(id);

        ApiResponse<ProductDetailResponse> apiResponse = ApiResponse.<ProductDetailResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('products.create')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @ModelAttribute @Valid ProductRequest request) throws IOException {
        ProductResponse response = productService.createProduct(request);

        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('products.update')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Integer id,
            @ModelAttribute @Valid ProductRequest request) throws IOException {
        ProductResponse response = productService.updateProduct(id, request);

        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('products.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Integer id) throws IOException {
        productService.deleteProduct(id);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .result(null)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
