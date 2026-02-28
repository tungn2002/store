package com.personal.store_api.service;

import com.personal.store_api.dto.request.StoreSettingsRequest;
import com.personal.store_api.dto.response.StoreSettingsResponse;
import com.personal.store_api.entity.StoreSettings;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.StoreSettingsMapper;
import com.personal.store_api.repository.StoreSettingsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoreSettingsService {

    StoreSettingsRepository storeSettingsRepository;
    StoreSettingsMapper storeSettingsMapper;

    @Transactional(readOnly = true)
    public StoreSettingsResponse getStoreSettings() {
        StoreSettings settings = storeSettingsRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new AppException(ErrorCode.STORE_SETTINGS_NOT_FOUND));

        return storeSettingsMapper.toStoreSettingsResponse(settings);
    }

    @Transactional
    public StoreSettingsResponse updateStoreSettings(StoreSettingsRequest request) {
        StoreSettings settings = storeSettingsRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new AppException(ErrorCode.STORE_SETTINGS_NOT_FOUND));

        storeSettingsMapper.updateStoreSettingsFromRequest(settings, request);
        StoreSettings updatedSettings = storeSettingsRepository.save(settings);

        return storeSettingsMapper.toStoreSettingsResponse(updatedSettings);
    }
}
