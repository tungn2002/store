package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.UserResponse;
import com.personal.store_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user management operations.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get paginated list of users with optional email filter.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('users.read')")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String email) {
        PaginatedResponse<UserResponse> response = userService.getUsers(page, size, sortBy, email);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Delete user by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
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
