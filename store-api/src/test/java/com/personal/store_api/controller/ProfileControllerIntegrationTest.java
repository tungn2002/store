package com.personal.store_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.store_api.dto.request.UpdateProfileRequest;
import com.personal.store_api.entity.Role;
import com.personal.store_api.entity.User;
import com.personal.store_api.enums.Gender;
import com.personal.store_api.repository.RoleRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ProfileController
 *
 * Tests cover:
 * 1. GET /profile - successful retrieval, user not found
 * 2. PUT /profile - successful update, email existed, user not found
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
public class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Clean up before each test - delete all users but keep roles
        userRepository.deleteAll();
        
        // Find or create test role with permissions - use entity graph to fetch permissions
        Role userRole = roleRepository.findByNameWithPermissions("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("{noop}password")
                .phoneNumber("0123456789")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .address("123 Test Street")
                .roles(Set.of(userRole))
                .build();
        
        testUser = userRepository.save(testUser);

        // Generate JWT token for test user using UserPrincipal
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
        authToken = jwtTokenProvider.generateToken(authentication);
    }

    // ==================== GET PROFILE TESTS ====================

    @Test
    void testGetProfile_WithValidJwt_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/profile")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(testUser.getId()))
                .andExpect(jsonPath("$.result.name").value("Test User"))
                .andExpect(jsonPath("$.result.email").value("test@example.com"))
                .andExpect(jsonPath("$.result.phoneNumber").value("0123456789"))
                .andExpect(jsonPath("$.result.dateOfBirth").value("1990-01-01"))
                .andExpect(jsonPath("$.result.gender").value("MALE"))
                .andExpect(jsonPath("$.result.address").value("123 Test Street"));
    }

    @Test
    void testGetProfile_WithNonExistentUser_ShouldReturnError() throws Exception {
        // Delete the user to simulate non-existent user
        userRepository.delete(testUser);

        mockMvc.perform(get("/profile")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1009))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Test
    void testUpdateProfile_WithValidData_ShouldReturnSuccess() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .phoneNumber("0987654321")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .gender(Gender.FEMALE)
                .address("456 Updated Avenue")
                .build();

        mockMvc.perform(put("/profile")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.name").value("Updated Name"))
                .andExpect(jsonPath("$.result.email").value("updated@example.com"))
                .andExpect(jsonPath("$.result.phoneNumber").value("0987654321"))
                .andExpect(jsonPath("$.result.dateOfBirth").value("1995-05-15"))
                .andExpect(jsonPath("$.result.gender").value("FEMALE"))
                .andExpect(jsonPath("$.result.address").value("456 Updated Avenue"));
    }

    @Test
    void testUpdateProfile_WithExistedEmail_ShouldReturnError() throws Exception {
        // Create another user with different email
        Role userRole = roleRepository.findAll().stream().findFirst().orElse(null);
        User anotherUser = User.builder()
                .name("Another User")
                .email("another@example.com")
                .password("{noop}password")
                .roles(Set.of(userRole))
                .build();
        userRepository.save(anotherUser);

        // Try to update test user's email to the existed email
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Updated Name")
                .email("another@example.com")  // This email already exists
                .build();

        mockMvc.perform(put("/profile")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testUpdateProfile_WithNonExistentUser_ShouldReturnError() throws Exception {
        // Delete the user to simulate non-existent user
        userRepository.delete(testUser);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        mockMvc.perform(put("/profile")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1009))
                .andExpect(jsonPath("$.message").exists());
    }
}
