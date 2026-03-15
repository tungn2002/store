package com.personal.store_api.service;

import com.personal.store_api.entity.Permission;
import com.personal.store_api.entity.Role;
import com.personal.store_api.repository.PermissionRepository;
import com.personal.store_api.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    /**
     * All API permissions for ADMIN role
     */
    private static final List<ApiPermission> ADMIN_PERMISSIONS = List.of(
            // Auth endpoints (ADMIN only)
            ApiPermission.of("auth.token", "Get Token"),
            ApiPermission.of("auth.token2", "Get Token 2 (Admin)"),

            // Brand endpoints
            ApiPermission.of("brands.read", "Read Brands"),
            ApiPermission.of("brands.create", "Create Brand"),
            ApiPermission.of("brands.update", "Update Brand"),
            ApiPermission.of("brands.delete", "Delete Brand"),

            // Cart endpoints
            ApiPermission.of("cart.read", "Read Cart"),
            ApiPermission.of("cart.read_variants", "Read Cart Variants"),
            ApiPermission.of("cart.add", "Add to Cart"),
            ApiPermission.of("cart.update", "Update Cart"),
            ApiPermission.of("cart.update_variant", "Update Cart Variant"),
            ApiPermission.of("cart.delete", "Delete Cart Item"),
            ApiPermission.of("cart.clear", "Clear Cart"),

            // Category endpoints
            ApiPermission.of("categories.read", "Read Categories"),
            ApiPermission.of("categories.create", "Create Category"),
            ApiPermission.of("categories.update", "Update Category"),
            ApiPermission.of("categories.delete", "Delete Category"),

            // Checkout endpoints
            ApiPermission.of("checkout.create", "Create Checkout"),

            // Order endpoints
            ApiPermission.of("orders.my_orders", "Read My Orders"),
            ApiPermission.of("orders.admin_all", "Read All Orders (Admin)"),
            ApiPermission.of("orders.read", "Read Order Details"),

            // Product endpoints
            ApiPermission.of("products.read", "Read Products"),
            ApiPermission.of("products.read_one", "Read Single Product"),
            ApiPermission.of("products.create", "Create Product"),
            ApiPermission.of("products.update", "Update Product"),
            ApiPermission.of("products.delete", "Delete Product"),

            // Product Variant endpoints
            ApiPermission.of("variants.read", "Read Product Variants"),
            ApiPermission.of("variants.read_one", "Read Single Variant"),
            ApiPermission.of("variants.create", "Create Variant"),
            ApiPermission.of("variants.update", "Update Variant"),
            ApiPermission.of("variants.delete", "Delete Variant"),

            // Profile endpoints
            ApiPermission.of("profile.read", "Read Profile"),
            ApiPermission.of("profile.update", "Update Profile"),
            ApiPermission.of("profile.change_password", "Change Password"),

            // Search endpoints
            ApiPermission.of("search.reindex", "Reindex Products (Admin)"),

            // Store Settings endpoints
            ApiPermission.of("store_settings.read", "Read Store Settings"),
            ApiPermission.of("store_settings.update", "Update Store Settings"),

            // Stripe Webhook endpoints
            ApiPermission.of("webhook.stripe", "Stripe Webhook"),

            // User endpoints (ADMIN only)
            ApiPermission.of("users.read", "Read Users"),
            ApiPermission.of("users.delete", "Delete User")
    );

    /**
     * Permissions for USER role (based on client endpoints)
     */
    private static final List<ApiPermission> USER_PERMISSIONS = List.of(
            // Cart endpoints
            ApiPermission.of("cart.read", "Read Cart"),
            ApiPermission.of("cart.read_variants", "Read Cart Variants"),
            ApiPermission.of("cart.add", "Add to Cart"),
            ApiPermission.of("cart.update", "Update Cart"),
            ApiPermission.of("cart.update_variant", "Update Cart Variant"),
            ApiPermission.of("cart.delete", "Delete Cart Item"),
            ApiPermission.of("cart.clear", "Clear Cart"),

            // Checkout endpoints
            ApiPermission.of("checkout.create", "Create Checkout"),

            // Order endpoints (own orders only)
            ApiPermission.of("orders.my_orders", "Read My Orders"),
            ApiPermission.of("orders.read", "Read Order Details"),

            // Profile endpoints
            ApiPermission.of("profile.read", "Read Profile"),
            ApiPermission.of("profile.update", "Update Profile"),
            ApiPermission.of("profile.change_password", "Change Password")
    );

    @Transactional
    public void initializePermissions() {
        log.info("Initializing permissions...");

        // Create all permissions if not exist
        List<Permission> allPermissions = new ArrayList<>();
        for (ApiPermission apiPerm : ADMIN_PERMISSIONS) {
            Permission permission = permissionRepository.findByName(apiPerm.name)
                    .orElseGet(() -> {
                        Permission newPerm = Permission.builder()
                                .name(apiPerm.name)
                                .displayName(apiPerm.displayName)
                                .build();
                        return permissionRepository.save(newPerm);
                    });
            allPermissions.add(permission);
        }

        // Add USER permissions that might not be in ADMIN list
        Set<String> adminPermNames = new HashSet<>();
        ADMIN_PERMISSIONS.forEach(p -> adminPermNames.add(p.name));

        for (ApiPermission apiPerm : USER_PERMISSIONS) {
            if (!adminPermNames.contains(apiPerm.name)) {
                Permission permission = permissionRepository.findByName(apiPerm.name)
                        .orElseGet(() -> {
                            Permission newPerm = Permission.builder()
                                    .name(apiPerm.name)
                                    .displayName(apiPerm.displayName)
                                    .build();
                            return permissionRepository.save(newPerm);
                        });
                allPermissions.add(permission);
            }
        }

        log.info("Created {} permissions", allPermissions.size());

        // Assign all permissions to ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        Set<Permission> adminPermissions = new HashSet<>(allPermissions);
        adminRole.setPermissions(adminPermissions);
        roleRepository.save(adminRole);
        log.info("Assigned {} permissions to ADMIN role", adminPermissions.size());

        // Assign USER permissions to USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        Set<Permission> userPermissions = new HashSet<>();
        for (ApiPermission apiPerm : USER_PERMISSIONS) {
            permissionRepository.findByName(apiPerm.name).ifPresent(userPermissions::add);
        }
        userRole.setPermissions(userPermissions);
        roleRepository.save(userRole);
        log.info("Assigned {} permissions to USER role", userPermissions.size());

        log.info("Permission initialization completed.");
    }

    private record ApiPermission(String name, String displayName) {
        static ApiPermission of(String name, String displayName) {
            return new ApiPermission(name, displayName);
        }
    }
}
