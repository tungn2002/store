package com.personal.store_api.mapper;

import com.personal.store_api.dto.request.BrandRequest;
import com.personal.store_api.dto.response.BrandResponse;
import com.personal.store_api.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BrandMapper {

    BrandResponse toBrandResponse(Brand brand);

    void updateBrandFromRequest(@MappingTarget Brand brand, BrandRequest request);
}
