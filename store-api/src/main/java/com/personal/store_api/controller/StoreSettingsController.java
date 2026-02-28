package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.StoreSettingsRequest;
import com.personal.store_api.dto.response.StoreSettingsResponse;
import com.personal.store_api.service.StoreSettingsService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store-settings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoreSettingsController {

    StoreSettingsService storeSettingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> getStoreSettings() {
        StoreSettingsResponse response = storeSettingsService.getStoreSettings();

        ApiResponse<StoreSettingsResponse> apiResponse = ApiResponse.<StoreSettingsResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> updateStoreSettings(
            @RequestBody @Valid StoreSettingsRequest request) {
        StoreSettingsResponse response = storeSettingsService.updateStoreSettings(request);

        ApiResponse<StoreSettingsResponse> apiResponse = ApiResponse.<StoreSettingsResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
