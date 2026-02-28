package com.personal.store_api.mapper;

import com.personal.store_api.dto.request.StoreSettingsRequest;
import com.personal.store_api.dto.response.StoreSettingsResponse;
import com.personal.store_api.entity.StoreSettings;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StoreSettingsMapper {

    StoreSettingsResponse toStoreSettingsResponse(StoreSettings settings);

    void updateStoreSettingsFromRequest(@MappingTarget StoreSettings settings, StoreSettingsRequest request);
}
