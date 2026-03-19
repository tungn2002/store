package com.personal.store_api.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.store_api.repository.*;
import com.personal.store_api.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base integration test class with Testcontainers support.
 * Provides common test infrastructure for all integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    static {
        // Ensure containers are started before any test runs
        ContainersConfig.ensureContainersStarted();
    }

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        ContainersConfig.registerProperties(registry);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected BrandRepository brandRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected ProductVariantRepository productVariantRepository;

    @Autowired
    protected CartRepository cartRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected StoreSettingsRepository storeSettingsRepository;

    @Autowired
    protected CategoryService categoryService;

    @Autowired
    protected BrandService brandService;

    @Autowired
    protected ProductService productService;

    @Autowired
    protected CartService cartService;

    @Autowired
    protected OrderService orderService;

    @Autowired
    protected AuthService authService;

    @Autowired
    protected TestDataUtil testDataUtil;
}
