package com.personal.store_api.controller;

import com.personal.store_api.entity.*;
import com.personal.store_api.repository.*;
import com.personal.store_api.security.JwtTokenProvider;
import com.personal.store_api.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class CartControllerIntegrationTest {

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
    private CartRepository cartRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        cartRepository.deleteAll();
        productVariantRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        SecurityContextHolder.clearContext();

        Role userRole = roleRepository.save(Role.builder()
                .name("USER")
                .displayName("User")
                .permissions(new HashSet<>())
                .build());

        testUser = userRepository.save(User.builder()
                .name("Test User")
                .email("testuser@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());

        UserPrincipal userPrincipal = UserPrincipal.create(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authToken = jwtTokenProvider.generateToken(authentication);
    }

    @Test
    void testAddToCart_WithValidData_ShouldReturnSuccess() throws Exception {
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

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("M")
                .color("Red")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(50)
                .build());

        Map<String, Object> request = new HashMap<>();
        request.put("productVariantId", variant.getId());
        request.put("quantity", 1);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.productVariantId").value(variant.getId()))
                .andExpect(jsonPath("$.result.quantity").value(1))
                .andExpect(jsonPath("$.result.price").value(1000000));
    }

    @Test
    void testAddToCart_WithDefaultQuantity1_ShouldReturnSuccess() throws Exception {
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

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("M")
                .color("Red")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(50)
                .build());

        Map<String, Object> request = new HashMap<>();
        request.put("productVariantId", variant.getId());
        request.put("quantity", 1);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.quantity").value(1));
    }

    @Test
    void testAddToCart_WithQuantityExceedsStock_ShouldReturnError() throws Exception {
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

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("M")
                .color("Red")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(10)
                .build());

        Map<String, Object> request = new HashMap<>();
        request.put("productVariantId", variant.getId());
        request.put("quantity", 15);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddToCart_WithQuantityOver100_ShouldReturnError() throws Exception {
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

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("M")
                .color("Red")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(200)
                .build());

        Map<String, Object> request = new HashMap<>();
        request.put("productVariantId", variant.getId());
        request.put("quantity", 101);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddToCart_WithOutOfStockProduct_ShouldReturnError() throws Exception {
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

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("M")
                .color("Red")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(0)
                .build());

        Map<String, Object> request = new HashMap<>();
        request.put("productVariantId", variant.getId());
        request.put("quantity", 1);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddToCart_WithDuplicateProductVariant_ShouldReturnError() throws Exception {
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

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("M")
                .color("Red")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(50)
                .build());

        Map<String, Object> request = new HashMap<>();
        request.put("productVariantId", variant.getId());
        request.put("quantity", 1);

        // Add to cart first time - should succeed
        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Add same product variant second time - should fail
        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
