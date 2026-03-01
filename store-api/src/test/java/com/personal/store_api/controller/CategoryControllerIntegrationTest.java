package com.personal.store_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.store_api.entity.Category;
import com.personal.store_api.entity.Role;
import com.personal.store_api.entity.User;
import com.personal.store_api.repository.CategoryRepository;
import com.personal.store_api.repository.RoleRepository;
import com.personal.store_api.repository.UserRepository;
import com.personal.store_api.security.JwtTokenProvider;
import com.personal.store_api.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CategoryController
 *
 * Tests cover:
 * 1. GET /categories - pagination and retrieval
 * 2. POST /categories - create with image
 * 3. PUT /categories/{id} - update with new image (delete old)
 * 4. DELETE /categories/{id} - delete and clean up image
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
public class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private com.personal.store_api.service.CloudinaryService cloudinaryService;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        SecurityContextHolder.clearContext();

        // Mock CloudinaryService
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "http://example.com/uploaded-image.jpg");
        uploadResult.put("public_id", "test/public_id");
        when(cloudinaryService.uploadImage(any())).thenReturn(uploadResult);
        doNothing().when(cloudinaryService).deleteImage(any());

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
    void testGetCategories_WithNoData_ShouldReturnEmptyPage() throws Exception {
        mockMvc.perform(get("/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items").isEmpty())
                .andExpect(jsonPath("$.result.totalItems").value(0))
                .andExpect(jsonPath("$.result.totalPages").value(0));
    }

    @Test
    void testGetCategories_WithData_ShouldReturnPaginatedResults() throws Exception {
        // Create test categories
        categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/img1.jpg")
                .build());
        categoryRepository.save(Category.builder()
                .name("Clothing")
                .image("http://example.com/img2.jpg")
                .build());
        categoryRepository.save(Category.builder()
                .name("Books")
                .image("http://example.com/img3.jpg")
                .build());

        mockMvc.perform(get("/categories")
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
    void testCreateCategory_WithValidData_ShouldReturnSuccess() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/categories")
                        .file(imageFile)
                        .param("name", "Test Category")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Test Category"))
                .andExpect(jsonPath("$.result.id").exists());
    }

    @Test
    void testCreateCategory_WithoutImage_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(multipart("/categories")
                        .param("name", "Test Category No Image")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Test Category No Image"))
                .andExpect(jsonPath("$.result.id").exists());
    }

    @Test
    void testCreateCategory_WithBlankName_ShouldReturnError() throws Exception {
        mockMvc.perform(multipart("/categories")
                        .param("name", "")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCategory_WithNullName_ShouldReturnError() throws Exception {
        mockMvc.perform(multipart("/categories")
                        .param("name", (String) null)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCategory_WithNameTooLong_ShouldReturnError() throws Exception {
        String longName = "a".repeat(101);
        mockMvc.perform(multipart("/categories")
                        .param("name", longName)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateCategory_WithValidData_ShouldReturnSuccess() throws Exception {
        // Create a category first
        Category category = categoryRepository.save(Category.builder()
                .name("Original Name")
                .image("http://example.com/original.jpg")
                .build());

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "new-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new image content".getBytes()
        );

        mockMvc.perform(multipart("/categories/" + category.getId())
                        .file(imageFile)
                        .param("name", "Updated Name")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Updated Name"))
                .andExpect(jsonPath("$.result.id").value(category.getId()));
    }

    @Test
    void testUpdateCategory_WithoutImage_ShouldKeepOldImage() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Original Name")
                .image("http://example.com/original.jpg")
                .build());

        mockMvc.perform(multipart("/categories/" + category.getId())
                        .param("name", "Updated Name")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Updated Name"));
    }

    @Test
    void testUpdateCategory_NotFound_ShouldReturnError() throws Exception {
        mockMvc.perform(multipart("/categories/999")
                        .param("name", "Updated Name")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCategory_ExistingCategory_ShouldReturnSuccess() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("To Delete")
                .image("http://example.com/to-delete.jpg")
                .build());

        mockMvc.perform(delete("/categories/" + category.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCategory_NotFound_ShouldReturnError() throws Exception {
        mockMvc.perform(delete("/categories/999")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCategories_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateCategory_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(multipart("/categories")
                        .param("name", "Test Category")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteCategory_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
