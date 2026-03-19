package com.personal.store_api.infrastructure;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.personal.store_api.entity.*;
import com.personal.store_api.enums.Gender;
import com.personal.store_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

/**
 * Utility class for test data creation and JWT token generation.
 */
@Component
public class TestDataUtil {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private static final String JWT_SECRET = "fd0874c323c8f2fb6c59e865fb22ad5f70f43604ddecc1fb7ca2e2489d5d97c0";

    /**
     * Create a test user with the given email.
     */
    public User createUser(String email) {
        return createUser(email, "roles.read", "categories.read", "brands.read", "products.read",
                "cart.read", "cart.add", "cart.update", "cart.delete", "orders.my_orders",
                "profile.read", "profile.update", "profile.change_password");
    }

    /**
     * Create a test admin user with all permissions.
     */
    public User createAdminUser(String email) {
        return createUser(email, "roles.*", "categories.*", "brands.*", "products.*",
                "cart.*", "orders.*", "users.*", "profile.*", "store_settings.*",
                "auth.*", "checkout.*");
    }

    /**
     * Create a test user with specified authorities.
     */
    public User createUser(String email, String... authorities) {
        // Generate unique email to avoid duplicates
        String uniqueEmail = email + "_" + System.nanoTime();
        
        Set<Role> roles = new HashSet<>();
        if (authorities.length > 0) {
            // Create role with unique name using nanoTime and random suffix
            String roleName = "ROLE_USER_" + System.nanoTime() + "_" + (int)(Math.random() * 10000);
            Role role = Role.builder()
                    .name(roleName)
                    .permissions(new HashSet<>())
                    .build();
            role = roleRepository.save(role);
            roles.add(role);
        }

        User user = User.builder()
                .email(uniqueEmail)
                .name("Test User")
                .password("$2a$10$N5XKj6cX5zW5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q")
                .phoneNumber("0123456789")
                .gender(Gender.MALE)
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    /**
     * Create a test category.
     */
    public Category createCategory(String name) {
        Category category = Category.builder()
                .name(name)
                .image("https://res.cloudinary.com/test/image/upload/v1/test.jpg")
                .build();
        return categoryRepository.save(category);
    }

    /**
     * Create a test brand.
     */
    public Brand createBrand(String name) {
        Brand brand = Brand.builder()
                .name(name)
                .build();
        return brandRepository.save(brand);
    }

    /**
     * Create a test product.
     */
    public Product createProduct(String name, Category category, Brand brand) {
        Product product = Product.builder()
                .name(name)
                .description("Test product description")
                .category(category)
                .brand(brand)
                .build();
        return productRepository.save(product);
    }

    /**
     * Create a test product variant.
     */
    public ProductVariant createProductVariant(Product product, String size, String color, Integer stockQuantity) {
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(size)
                .color(color)
                .stockQuantity(stockQuantity)
                .price(java.math.BigDecimal.valueOf(100000))
                .build();
        return productVariantRepository.save(variant);
    }

    /**
     * Create a test cart item.
     */
    public Cart createCartItem(User user, ProductVariant variant, Integer quantity) {
        Cart cart = Cart.builder()
                .user(user)
                .productVariant(variant)
                .quantity(quantity)
                .build();
        return cartRepository.save(cart);
    }

    /**
     * Create a test order.
     */
    public Order createOrder(User user, java.math.BigDecimal totalAmount, Order.Status status) {
        Order order = Order.builder()
                .userId(user.getId())
                .customerName(user.getName())
                .customerEmail(user.getEmail())
                .customerPhone(user.getPhoneNumber())
                .totalAmount(totalAmount)
                .status(status)
                .items(new ArrayList<>())
                .build();
        return orderRepository.save(order);
    }

    /**
     * Generate a JWT token for the given user with specified authorities.
     */
    public String generateToken(User user, String... authorities) throws Exception {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Duration.ofHours(1).toMillis());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("test-issuer")
                .issueTime(now)
                .expirationTime(expiry)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", String.join(" ", authorities))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        signedJWT.sign(new MACSigner(JWT_SECRET));
        return signedJWT.serialize();
    }

    /**
     * Create a MockMvc request header with authorization token.
     */
    public String getAuthorizationHeader(User user, String... authorities) throws Exception {
        return "Bearer " + generateToken(user, authorities);
    }

    /**
     * Get the default test password (BCrypt encoded).
     */
    public static String getDefaultPassword() {
        return "$2a$10$N5XKj6cX5zW5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q5X5Q";
    }
}
