package com.personal.store_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.store_api.entity.Role;
import com.personal.store_api.entity.StoreSettings;
import com.personal.store_api.entity.User;
import com.personal.store_api.repository.RoleRepository;
import com.personal.store_api.repository.StoreSettingsRepository;
import com.personal.store_api.repository.UserRepository;
import com.personal.store_api.security.JwtTokenProvider;
import com.personal.store_api.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for StoreSettingsController
 *
 * Tests cover:
 * 1. GET /store-settings - successful retrieval
 * 2. PUT /store-settings - successful update
 *
 * Uses H2 in-memory database with automatic rollback
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "spring.h2.console.enabled=false"
})
public class StoreSettingsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;
    private StoreSettings storeSettings;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        storeSettingsRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        SecurityContextHolder.clearContext();

        // Create admin role with empty permissions set
        Role adminRole = roleRepository.save(Role.builder()
                .name("ADMIN")
                .displayName("Admin")
                .permissions(new HashSet<>())
                .build());

        // Create test user with admin role
        testUser = userRepository.save(User.builder()
                .name("Test Admin")
                .email("testadmin@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(adminRole))
                .build());

        // Generate JWT token
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authToken = jwtTokenProvider.generateToken(authentication);

        // Create default store settings
        storeSettings = storeSettingsRepository.save(StoreSettings.builder()
                .name("Test Store")
                .address("123 Test Street")
                .build());
    }

    @Test
    void testGetStoreSettings_WithAuthenticatedUser_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/store-settings")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Test Store"))
                .andExpect(jsonPath("$.result.address").value("123 Test Street"));
    }

    @Test
    void testUpdateStoreSettings_WithValidData_ShouldReturnSuccess() throws Exception {
        String newName = "Updated Store Name";
        String newAddress = "456 Updated Avenue";

        var request = new com.personal.store_api.dto.request.StoreSettingsRequest(
                newName, newAddress
        );

        mockMvc.perform(put("/store-settings")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value(newName))
                .andExpect(jsonPath("$.result.address").value(newAddress));
    }

    @Test
    void testUpdateStoreSettings_WithBlankName_ShouldReturnError() throws Exception {
        var request = new com.personal.store_api.dto.request.StoreSettingsRequest(
                "", "Valid Address"
        );

        mockMvc.perform(put("/store-settings")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateStoreSettings_WithNullName_ShouldReturnError() throws Exception {
        var request = new com.personal.store_api.dto.request.StoreSettingsRequest(
                null, "Valid Address"
        );

        mockMvc.perform(put("/store-settings")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateStoreSettings_WithAddressTooLong_ShouldReturnError() throws Exception {
        String longAddress = "a".repeat(501);
        var request = new com.personal.store_api.dto.request.StoreSettingsRequest(
                "Valid Name", longAddress
        );

        mockMvc.perform(put("/store-settings")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateStoreSettings_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Clear security context to ensure no authentication
        SecurityContextHolder.clearContext();
        
        var request = new com.personal.store_api.dto.request.StoreSettingsRequest(
                "New Name", "New Address"
        );

        mockMvc.perform(put("/store-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
