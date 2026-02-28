package com.personal.store_api.mapper;

import com.personal.store_api.dto.request.UpdateProfileRequest;
import com.personal.store_api.dto.response.ProfileResponse;
import com.personal.store_api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProfileMapper {

    ProfileResponse toProfileResponse(User user);

    void updateProfileResponse(@MappingTarget ProfileResponse response, User user);

    void updateUserFromRequest(@MappingTarget User user, UpdateProfileRequest request);
}
