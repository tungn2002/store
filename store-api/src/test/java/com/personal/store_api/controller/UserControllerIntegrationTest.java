package com.personal.store_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for UserController
 *
 * Tests cover:
 * 1. GET /users - pagination and search by email
 * 2. DELETE /users/{id} - delete user
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
public class UserControllerIntegrationTest {

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test
        userRepository.deleteAll();
        roleRepository.deleteAll();
        SecurityContextHolder.clearContext();

        // Create admin role
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
    }

    @Test
    void testGetUsers_WithNoData_ShouldReturnEmptyPage() throws Exception {
        // Admin user exists but should be filtered out
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items").isEmpty())
                .andExpect(jsonPath("$.result.totalItems").value(0))
                .andExpect(jsonPath("$.result.totalPages").value(0));
    }

    @Test
    void testGetUsers_WithData_ShouldReturnPaginatedResults() throws Exception {
        // Create test users
        Role userRole = roleRepository.save(Role.builder()
                .name("USER")
                .displayName("User")
                .permissions(new HashSet<>())
                .build());

        userRepository.save(User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());
        userRepository.save(User.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());
        userRepository.save(User.builder()
                .name("Bob Wilson")
                .email("bob@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "createdAt")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(2))
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.size").value(2))
                .andExpect(jsonPath("$.result.totalItems").value(2))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.hasNext").value(false));
    }

    @Test
    void testGetUsers_WithEmailSearch_ShouldReturnFilteredResults() throws Exception {
        // Create test users
        Role userRole = roleRepository.save(Role.builder()
                .name("USER")
                .displayName("User")
                .permissions(new HashSet<>())
                .build());

        userRepository.save(User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());
        userRepository.save(User.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());
        userRepository.save(User.builder()
                .name("Bob Wilson")
                .email("bob@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("email", "john")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(1))
                .andExpect(jsonPath("$.result.items[0].email").value("john@example.com"));
    }

    @Test
    void testGetUsers_WithEmailSearchCaseInsensitive_ShouldReturnFilteredResults() throws Exception {
        // Create test users
        Role userRole = roleRepository.save(Role.builder()
                .name("USER")
                .displayName("User")
                .permissions(new HashSet<>())
                .build());

        userRepository.save(User.builder()
                .name("John Doe")
                .email("JOHN@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("email", "john")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(1));
    }

    @Test
    void testDeleteUser_ExistingUser_ShouldReturnSuccess() throws Exception {
        // Create a user to delete
        Role userRole = roleRepository.save(Role.builder()
                .name("USER")
                .displayName("User")
                .permissions(new HashSet<>())
                .build());

        User userToDelete = userRepository.save(User.builder()
                .name("User To Delete")
                .email("delete@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());

        mockMvc.perform(delete("/users/" + userToDelete.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + authToken)
                        .param("email", "delete@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalItems").value(0));
    }

    @Test
    void testDeleteUser_AdminUser_ShouldReturnForbidden() throws Exception {
        // Try to delete the admin user (testUser)
        mockMvc.perform(delete("/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUser_NotFound_ShouldReturnError() throws Exception {
        mockMvc.perform(delete("/users/non-existent-id")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUsers_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteUser_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/users/some-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
