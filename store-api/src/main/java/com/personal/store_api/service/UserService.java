package com.personal.store_api.service;

import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.dto.response.UserResponse;
import com.personal.store_api.entity.User;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.UserMapper;
import com.personal.store_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    final UserRepository userRepository;
    final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponse> getUsers(int page, int size, String sortBy, String email) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));

        Page<User> userPage;
        if (email != null && !email.isEmpty()) {
            userPage = userRepository.findAllByEmailContainingIgnoreCase(email, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        // Filter out admin users
        java.util.List<User> nonAdminUsers = userPage.getContent().stream()
                .filter(user -> {
                    boolean isAdmin = user.getRoles().stream()
                            .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
                    return !isAdmin;
                })
                .collect(java.util.stream.Collectors.toList());

        // Create a new page with filtered results - totalElements should be the filtered count
        Page<User> filteredPage = new PageImpl<>(
                nonAdminUsers,
                pageable,
                nonAdminUsers.size()  // Only count non-admin users
        );

        return PaginatedResponse.<UserResponse>builder()
                .items(filteredPage.getContent().stream()
                        .map(userMapper::toUserResponse)
                        .collect(Collectors.toList()))
                .page(filteredPage.getNumber())
                .size(filteredPage.getSize())
                .totalItems(filteredPage.getTotalElements())
                .totalPages((int) Math.ceil((double) filteredPage.getTotalElements() / size))
                .isFirst(filteredPage.isFirst())
                .isLast(filteredPage.isLast())
                .hasNext(filteredPage.hasNext())
                .hasPrevious(filteredPage.hasPrevious())
                .build();
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Prevent deleting admin users
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        if (isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        userRepository.delete(user);
    }
}
