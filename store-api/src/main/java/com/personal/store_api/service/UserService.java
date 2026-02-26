package com.personal.store_api.service;

import com.personal.store_api.entity.Role;
import com.personal.store_api.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    RoleRepository roleRepository;

    public Set<Role> getDefaultRoles() {
        Set<Role> roles = new HashSet<>();
        roleRepository.findById("USER")
                .ifPresent(roles::add);
        return roles;
    }
}
