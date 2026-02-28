package com.personal.store_api.service;

import com.personal.store_api.dto.request.UpdateProfileRequest;
import com.personal.store_api.dto.response.ProfileResponse;
import com.personal.store_api.entity.User;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.ProfileMapper;
import com.personal.store_api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {

    UserRepository userRepository;
    ProfileMapper profileMapper;

    public ProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return profileMapper.toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Validate email not existed (if email is updated)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        throw new AppException(ErrorCode.USER_EXISTED);
                    });
        }

        profileMapper.updateUserFromRequest(user, request);
        User updatedUser = userRepository.save(user);

        return profileMapper.toProfileResponse(updatedUser);
    }
}
