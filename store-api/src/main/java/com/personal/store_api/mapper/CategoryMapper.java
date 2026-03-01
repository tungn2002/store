package com.personal.store_api.mapper;

import com.personal.store_api.dto.request.CategoryRequest;
import com.personal.store_api.dto.response.CategoryResponse;
import com.personal.store_api.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "image", ignore = true)
    void updateCategoryFromRequest(@MappingTarget Category category, CategoryRequest request);
}
