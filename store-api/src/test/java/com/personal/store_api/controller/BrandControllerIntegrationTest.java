package com.personal.store_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.store_api.entity.Brand;
import com.personal.store_api.entity.Role;
import com.personal.store_api.entity.User;
import com.personal.store_api.repository.BrandRepository;
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

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for BrandController
 *
 * Tests cover:
 * 1. GET /brands - pagination and retrieval
 * 2. POST /brands - create
 * 3. PUT /brands/{id} - update
 * 4. DELETE /brands/{id} - delete
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
public class BrandControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test
        brandRepository.deleteAll();
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
    }

    @Test
    void testGetBrands_WithNoData_ShouldReturnEmptyPage() throws Exception {
        mockMvc.perform(get("/brands")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items").isEmpty())
                .andExpect(jsonPath("$.result.totalItems").value(0))
                .andExpect(jsonPath("$.result.totalPages").value(0));
    }

    @Test
    void testGetBrands_WithData_ShouldReturnPaginatedResults() throws Exception {
        // Create test brands
        brandRepository.save(Brand.builder().name("Nike").build());
        brandRepository.save(Brand.builder().name("Adidas").build());
        brandRepository.save(Brand.builder().name("Puma").build());

        mockMvc.perform(get("/brands")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(2))
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.size").value(2))
                .andExpect(jsonPath("$.result.totalItems").value(3))
                .andExpect(jsonPath("$.result.totalPages").value(2))
                .andExpect(jsonPath("$.result.hasNext").value(true));
    }

    @Test
    void testCreateBrand_WithValidData_ShouldReturnSuccess() throws Exception {
        var request = new com.personal.store_api.dto.request.BrandRequest("Nike");

        mockMvc.perform(post("/brands")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Nike"))
                .andExpect(jsonPath("$.result.id").exists());
    }

    @Test
    void testCreateBrand_WithBlankName_ShouldReturnError() throws Exception {
        var request = new com.personal.store_api.dto.request.BrandRequest("");

        mockMvc.perform(post("/brands")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBrand_WithNullName_ShouldReturnError() throws Exception {
        var request = new com.personal.store_api.dto.request.BrandRequest((String) null);

        mockMvc.perform(post("/brands")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBrand_WithNameTooLong_ShouldReturnError() throws Exception {
        String longName = "a".repeat(101);
        var request = new com.personal.store_api.dto.request.BrandRequest(longName);

        mockMvc.perform(post("/brands")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBrand_WithValidData_ShouldReturnSuccess() throws Exception {
        // Create a brand first
        Brand brand = brandRepository.save(Brand.builder().name("Original Name").build());

        var request = new com.personal.store_api.dto.request.BrandRequest("Updated Name");

        mockMvc.perform(put("/brands/" + brand.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Updated Name"))
                .andExpect(jsonPath("$.result.id").value(brand.getId()));
    }

    @Test
    void testUpdateBrand_NotFound_ShouldReturnError() throws Exception {
        var request = new com.personal.store_api.dto.request.BrandRequest("Updated Name");

        mockMvc.perform(put("/brands/999")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBrand_ExistingBrand_ShouldReturnSuccess() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("To Delete").build());

        mockMvc.perform(delete("/brands/" + brand.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteBrand_NotFound_ShouldReturnError() throws Exception {
        mockMvc.perform(delete("/brands/999")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBrands_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/brands")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateBrand_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        var request = new com.personal.store_api.dto.request.BrandRequest("Test Brand");

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteBrand_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/brands/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
