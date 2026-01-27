package com.personal.store_api.security;

import com.personal.store_api.entity.User;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_EXISTED)
                );

        return UserPrincipal.create(user);
    }
}
