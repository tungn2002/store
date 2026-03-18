package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.CategoryRequest;
import com.personal.store_api.dto.response.CategoryResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * Controller for category management operations.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get paginated list of categories.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('categories.read')")
    public ResponseEntity<ApiResponse<PaginatedResponse<CategoryResponse>>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        PaginatedResponse<CategoryResponse> response = categoryService.getCategories(page, size, sortBy);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Get all categories (no pagination).
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Create a new category.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('categories.create')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @ModelAttribute @Valid CategoryRequest request) throws IOException {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Update an existing category.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('categories.update')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Integer id,
            @ModelAttribute @Valid CategoryRequest request) throws IOException {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Delete a category by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('categories.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) throws IOException {
        categoryService.deleteCategory(id);
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
