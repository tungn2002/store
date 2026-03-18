package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.ChangePasswordRequest;
import com.personal.store_api.dto.request.UpdateProfileRequest;
import com.personal.store_api.dto.response.ProfileResponse;
import com.personal.store_api.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user profile operations.
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get current user's profile.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('profile.read')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        String userId = getCurrentUserId();
        ProfileResponse profileResponse = profileService.getProfile(userId);
        return ResponseEntity.ok(buildResponse(profileResponse));
    }

    /**
     * Update current user's profile.
     */
    @PutMapping
    @PreAuthorize("hasAuthority('profile.update')")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request) {
        String userId = getCurrentUserId();
        ProfileResponse profileResponse = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(buildResponse(profileResponse));
    }

    /**
     * Change current user's password.
     */
    @PutMapping("/change-password")
    @PreAuthorize("hasAuthority('profile.change_password')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody @Valid ChangePasswordRequest request) {
        String userId = getCurrentUserId();
        profileService.changePassword(userId, request);
        return ResponseEntity.ok(buildResponse());
    }

    /**
     * Get current user ID from JWT token.
     */
    private String getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
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
