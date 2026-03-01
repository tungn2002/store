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

import java.math.BigDecimal;
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
public class ProductVariantControllerIntegrationTest {

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
    void testGetProductVariants_WithNoData_ShouldReturnEmptyPage() throws Exception {
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

        mockMvc.perform(get("/products/" + product.getId() + "/variants")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items").isEmpty())
                .andExpect(jsonPath("$.result.totalItems").value(0));
    }

    @Test
    void testCreateProductVariant_WithValidData_ShouldReturnSuccess() throws Exception {
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

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "variant.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        mockMvc.perform(multipart("/products/" + product.getId() + "/variants")
                        .file(imageFile)
                        .param("size", "M")
                        .param("color", "Red")
                        .param("price", "99.99")
                        .param("stockQuantity", "100")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.size").value("M"))
                .andExpect(jsonPath("$.result.color").value("Red"))
                .andExpect(jsonPath("$.result.price").value(99.99));
    }

    @Test
    void testCreateProductVariant_WithDuplicateSizeColor_ShouldReturnError() throws Exception {
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

        // Create first variant
        productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .size("M")
                .color("Red")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .build());

        // Try to create duplicate variant
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "variant.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        mockMvc.perform(multipart("/products/" + product.getId() + "/variants")
                        .file(imageFile)
                        .param("size", "M")
                        .param("color", "Red")
                        .param("price", "199.99")
                        .param("stockQuantity", "50")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A variant with this size and color combination already exists"));
    }

    @Test
    void testGetProductVariant_WithValidId_ShouldReturnVariant() throws Exception {
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
                .size("L")
                .color("Blue")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .image("http://example.com/variant.jpg")
                .build());

        mockMvc.perform(get("/products/" + product.getId() + "/variants/" + variant.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.size").value("L"))
                .andExpect(jsonPath("$.result.color").value("Blue"));
    }

    @Test
    void testUpdateProductVariant_WithValidData_ShouldReturnSuccess() throws Exception {
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
                .color("Green")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .image("http://example.com/variant.jpg")
                .build());

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "variant-updated.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        mockMvc.perform(multipart("/products/" + product.getId() + "/variants/" + variant.getId())
                        .file(imageFile)
                        .param("size", "XL")
                        .param("color", "Yellow")
                        .param("price", "199.99")
                        .param("stockQuantity", "75")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.size").value("XL"))
                .andExpect(jsonPath("$.result.color").value("Yellow"))
                .andExpect(jsonPath("$.result.price").value(199.99));
    }

    @Test
    void testDeleteProductVariant_WithValidId_ShouldReturnSuccess() throws Exception {
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
                .size("S")
                .color("Black")
                .price(new BigDecimal("79.99"))
                .stockQuantity(200)
                .image("http://example.com/variant.jpg")
                .build());

        mockMvc.perform(delete("/products/" + product.getId() + "/variants/" + variant.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductVariants_WithPagination_ShouldReturnPaginatedResults() throws Exception {
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

        // Create 5 variants
        for (int i = 1; i <= 5; i++) {
            productVariantRepository.save(ProductVariant.builder()
                    .product(product)
                    .size("Size" + i)
                    .color("Color" + i)
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(100)
                    .build());
        }

        mockMvc.perform(get("/products/" + product.getId() + "/variants")
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
}
