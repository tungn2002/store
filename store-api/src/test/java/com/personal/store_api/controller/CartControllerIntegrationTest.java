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

    @Test
    void testGetCart_WithItems_ShouldReturnCartWithItems() throws Exception {
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

        // Add item to cart first
        Map<String, Object> addToCartRequest = new HashMap<>();
        addToCartRequest.put("productVariantId", variant.getId());
        addToCartRequest.put("quantity", 2);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk());

        // Get cart
        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(1))
                .andExpect(jsonPath("$.result.items[0].quantity").value(2))
                .andExpect(jsonPath("$.result.items[0].subtotal").value(2000000))
                .andExpect(jsonPath("$.result.totalItems").value(1))
                .andExpect(jsonPath("$.result.subtotal").value(2000000))
                .andExpect(jsonPath("$.result.shippingFee").value(30000))
                .andExpect(jsonPath("$.result.total").value(2030000));
    }

    @Test
    void testGetCart_EmptyCart_ShouldReturnEmptyCart() throws Exception {
        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items.length()").value(0))
                .andExpect(jsonPath("$.result.totalItems").value(0))
                .andExpect(jsonPath("$.result.subtotal").value(0))
                .andExpect(jsonPath("$.result.shippingFee").value(30000))
                .andExpect(jsonPath("$.result.total").value(30000));
    }

    @Test
    void testUpdateCartItem_WithValidQuantity_ShouldReturnUpdatedItem() throws Exception {
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

        // Add item to cart first
        Map<String, Object> addToCartRequest = new HashMap<>();
        addToCartRequest.put("productVariantId", variant.getId());
        addToCartRequest.put("quantity", 1);

        String response = mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer cartId = objectMapper.readTree(response).get("result").get("id").asInt();

        // Update quantity
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("quantity", 5);

        mockMvc.perform(put("/cart/items/" + cartId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.quantity").value(5))
                .andExpect(jsonPath("$.result.subtotal").value(5000000));
    }

    @Test
    void testUpdateCartItem_WithQuantityZero_ShouldReturnError() throws Exception {
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

        // Add item to cart first
        Map<String, Object> addToCartRequest = new HashMap<>();
        addToCartRequest.put("productVariantId", variant.getId());
        addToCartRequest.put("quantity", 1);

        String response = mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer cartId = objectMapper.readTree(response).get("result").get("id").asInt();

        // Try to update with quantity 0 - should fail
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("quantity", 0);

        mockMvc.perform(put("/cart/items/" + cartId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCartItem_ExistingItem_ShouldReturnSuccess() throws Exception {
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

        // Add item to cart first
        Map<String, Object> addToCartRequest = new HashMap<>();
        addToCartRequest.put("productVariantId", variant.getId());
        addToCartRequest.put("quantity", 1);

        String response = mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer cartId = objectMapper.readTree(response).get("result").get("id").asInt();

        // Delete the cart item
        mockMvc.perform(delete("/cart/items/" + cartId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart item deleted successfully"));

        // Verify cart is empty
        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items.length()").value(0));
    }

    @Test
    void testClearCart_WithMultipleItems_ShouldClearAll() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .image("http://example.com/cat.jpg")
                .build());

        Brand brand = brandRepository.save(Brand.builder()
                .name("Nike")
                .build());

        Product product1 = productRepository.save(Product.builder()
                .name("Test Product 1")
                .description("Test Description 1")
                .category(category)
                .brand(brand)
                .image("http://example.com/product1.jpg")
                .build());

        Product product2 = productRepository.save(Product.builder()
                .name("Test Product 2")
                .description("Test Description 2")
                .category(category)
                .brand(brand)
                .image("http://example.com/product2.jpg")
                .build());

        ProductVariant variant1 = productVariantRepository.save(ProductVariant.builder()
                .product(product1)
                .size("M")
                .color("Red")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(50)
                .build());

        ProductVariant variant2 = productVariantRepository.save(ProductVariant.builder()
                .product(product2)
                .size("L")
                .color("Blue")
                .price(BigDecimal.valueOf(2000000))
                .stockQuantity(30)
                .build());

        // Add two items to cart
        Map<String, Object> addToCartRequest1 = new HashMap<>();
        addToCartRequest1.put("productVariantId", variant1.getId());
        addToCartRequest1.put("quantity", 1);

        Map<String, Object> addToCartRequest2 = new HashMap<>();
        addToCartRequest2.put("productVariantId", variant2.getId());
        addToCartRequest2.put("quantity", 2);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest2)))
                .andExpect(status().isOk());

        // Verify cart has 2 items
        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items.length()").value(2));

        // Clear cart
        mockMvc.perform(delete("/cart")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));

        // Verify cart is empty
        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items.length()").value(0));
    }
}
