package com.personal.store_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.store_api.entity.*;
import com.personal.store_api.repository.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

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
        productVariantRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        SecurityContextHolder.clearContext();

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "http://example.com/uploaded-image.jpg");
        uploadResult.put("public_id", "test/public_id");
        when(cloudinaryService.uploadImage(any())).thenReturn(uploadResult);
        doNothing().when(cloudinaryService).deleteImage(any());

        Role adminRole = roleRepository.save(Role.builder()
                .name("ADMIN")
                .displayName("Admin")
                .permissions(new HashSet<>())
                .build());

        testUser = userRepository.save(User.builder()
                .name("Test Admin")
                .email("testadmin@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(adminRole))
                .build());

        UserPrincipal userPrincipal = UserPrincipal.create(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authToken = jwtTokenProvider.generateToken(authentication);
    }

    @Test
    void testGetProducts_WithNoData_ShouldReturnEmptyPage() throws Exception {
        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items").isEmpty())
                .andExpect(jsonPath("$.result.totalItems").value(0));
    }

    @Test
    void testGetProducts_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        // Create 5 products
        for (int i = 1; i <= 5; i++) {
            productRepository.save(Product.builder()
                    .name("Product " + i)
                    .description("Description " + i)
                    .category(category)
                    .brand(brand)
                    .image("http://example.com/product" + i + ".jpg")
                    .build());
        }

        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(2))
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.size").value(2))
                .andExpect(jsonPath("$.result.totalItems").value(5))
                .andExpect(jsonPath("$.result.totalPages").value(3))
                .andExpect(jsonPath("$.result.hasNext").value(true));
    }

    @Test
    void testGetProducts_WithNameSearch_ShouldReturnFilteredResults() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        productRepository.save(Product.builder()
                .name("iPhone 15")
                .description("Apple phone")
                .category(category)
                .brand(brand)
                .build());

        productRepository.save(Product.builder()
                .name("Samsung Galaxy")
                .description("Samsung phone")
                .category(category)
                .brand(brand)
                .build());

        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + authToken)
                        .param("name", "iphone")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(1))
                .andExpect(jsonPath("$.result.items[0].name").value("iPhone 15"));
    }

    @Test
    void testGetProducts_WithCategoryFilter_ShouldReturnFilteredResults() throws Exception {
        Category category1 = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat1.jpg")
                .build());

        Category category2 = categoryRepository.save(Category.builder()
                .name("Clothing")
                .image("http://example.com/cat2.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        productRepository.save(Product.builder()
                .name("Phone")
                .category(category1)
                .brand(brand)
                .build());

        productRepository.save(Product.builder()
                .name("Shirt")
                .category(category2)
                .brand(brand)
                .build());

        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + authToken)
                        .param("categoryId", category1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(1))
                .andExpect(jsonPath("$.result.items[0].category.name").value("Electronics"));
    }

    @Test
    void testCreateProduct_WithValidData_ShouldReturnSuccess() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "product.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        mockMvc.perform(multipart("/products")
                        .file(imageFile)
                        .param("name", "Test Product")
                        .param("description", "Test Description")
                        .param("categoryId", category.getId().toString())
                        .param("brandId", brand.getId().toString())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Test Product"));
    }

    @Test
    void testCreateProduct_WithBlankName_ShouldReturnError() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        mockMvc.perform(multipart("/products")
                        .param("name", "")
                        .param("categoryId", category.getId().toString())
                        .param("brandId", brand.getId().toString())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateProduct_WithInvalidCategory_ShouldReturnError() throws Exception {
        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        mockMvc.perform(multipart("/products")
                        .param("name", "Test Product")
                        .param("categoryId", "999")
                        .param("brandId", brand.getId().toString())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetProduct_WithValidId_ShouldReturnProduct() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Test Product")
                .description("Test Description")
                .category(category)
                .brand(brand)
                .image("http://example.com/product.jpg")
                .build());

        mockMvc.perform(get("/products/" + product.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Test Product"))
                .andExpect(jsonPath("$.result.category.name").value("Electronics"));
    }

    @Test
    void testGetProduct_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/products/999")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateProduct_WithValidData_ShouldReturnSuccess() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Old Name")
                .description("Old Description")
                .category(category)
                .brand(brand)
                .image("http://example.com/old.jpg")
                .build());

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "product-new.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        mockMvc.perform(multipart("/products/" + product.getId())
                        .file(imageFile)
                        .param("name", "Updated Name")
                        .param("description", "Updated Description")
                        .param("categoryId", category.getId().toString())
                        .param("brandId", brand.getId().toString())
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
    void testUpdateProduct_WithInvalidId_ShouldReturnNotFound() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        mockMvc.perform(multipart("/products/999")
                        .param("name", "Updated Name")
                        .param("categoryId", category.getId().toString())
                        .param("brandId", brand.getId().toString())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProduct_WithValidId_ShouldReturnSuccess() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Test Product")
                .description("Test Description")
                .category(category)
                .brand(brand)
                .image("http://example.com/product.jpg")
                .build());

        mockMvc.perform(delete("/products/" + product.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteProduct_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/products/999")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetProduct_WithVariants_ShouldReturnProductWithVariants() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Test Product")
                .description("Test Description")
                .category(category)
                .brand(brand)
                .image("http://example.com/product.jpg")
                .build());

        productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("M")
                .color("Red")
                .build());

        productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("L")
                .color("Blue")
                .build());

        mockMvc.perform(get("/products/" + product.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Test Product"))
                .andExpect(jsonPath("$.result.variants").isArray())
                .andExpect(jsonPath("$.result.variants.length()").value(2));
    }
}
