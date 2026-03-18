package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.StoreSettingsRequest;
import com.personal.store_api.dto.response.StoreSettingsResponse;
import com.personal.store_api.service.StoreSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for store settings operations.
 */
@RestController
@RequestMapping("/store-settings")
@RequiredArgsConstructor
public class StoreSettingsController {

    private final StoreSettingsService storeSettingsService;

    /**
     * Get current store settings.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('store_settings.read')")
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> getStoreSettings() {
        StoreSettingsResponse response = storeSettingsService.getStoreSettings();
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Update store settings.
     */
    @PutMapping
    @PreAuthorize("hasAuthority('store_settings.update')")
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> updateStoreSettings(
            @RequestBody @Valid StoreSettingsRequest request) {
        StoreSettingsResponse response = storeSettingsService.updateStoreSettings(request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Build success response with result.
     */
    private <T> ApiResponse<T> buildResponse(T result) {
        return ApiResponse.<T>builder()
                .result(result)
                .build();
    }
}
