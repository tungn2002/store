package com.personal.store_api.infrastructure;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Test configuration for Testcontainers.
 * Provides PostgreSQL and Redis containers for integration tests.
 * Containers are started once and reused across all tests.
 */
public class ContainersConfig {

    // PostgreSQL Container - static singleton
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // Redis Container - static singleton
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private static boolean started = false;

    /**
     * Ensure containers are started (called from static initializer).
     */
    public static void ensureContainersStarted() {
        if (!started) {
            synchronized (ContainersConfig.class) {
                if (!started) {
                    System.out.println("Starting Testcontainers...");
                    postgreSQLContainer.start();
                    redisContainer.start();
                    started = true;
                    System.out.println("Testcontainers started successfully");
                }
            }
        }
    }

    /**
     * Register container properties for Spring.
     */
    public static void registerProperties(DynamicPropertyRegistry registry) {
        ensureContainersStarted();
        
        // PostgreSQL properties
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Redis properties
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redisContainer.getMappedPort(6379)));
        registry.add("spring.cache.type", () -> "redis");
    }
}
