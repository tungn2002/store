package com.personal.store_api.repository;

import com.personal.store_api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByBrandId(@Param("brandId") Integer brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByCategoryIdAndBrandId(@Param("categoryId") Integer categoryId,
                                              @Param("brandId") Integer brandId,
                                              Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByNameContainingIgnoreCaseAndCategoryId(@Param("name") String name,
                                                               @Param("categoryId") Integer categoryId,
                                                               Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByNameContainingIgnoreCaseAndBrandId(@Param("name") String name,
                                                            @Param("brandId") Integer brandId,
                                                            Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByNameContainingIgnoreCaseAndCategoryIdAndBrandId(@Param("name") String name,
                                                                         @Param("categoryId") Integer categoryId,
                                                                         @Param("brandId") Integer brandId,
                                                                         Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") Integer id);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.variants v " +
           "WHERE (:query IS NULL OR :query = '' OR p.name LIKE %:query%) " +
           "AND (:brandName IS NULL OR :brandName = '' OR b.name = :brandName) " +
           "AND (:categoryName IS NULL OR :categoryName = '' OR c.name = :categoryName) " +
           "AND (v.price IS NULL OR v.price >= :minPrice) " +
           "AND (v.price IS NULL OR v.price <= :maxPrice)")
    Page<Product> searchWithFilters(
            @Param("query") String query,
            @Param("brandName") String brandName,
            @Param("categoryName") String categoryName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.variants v " +
           "WHERE (:query IS NULL OR :query = '' OR p.name LIKE %:query%) " +
           "AND (:brandName IS NULL OR :brandName = '' OR b.name = :brandName) " +
           "AND (:categoryName IS NULL OR :categoryName = '' OR c.name = :categoryName) " +
           "AND (v.price IS NULL OR v.price >= :minPrice) " +
           "AND (v.price IS NULL OR v.price <= :maxPrice) " +
           "ORDER BY v.price ASC")
    Page<Product> searchWithFiltersPriceAsc(
            @Param("query") String query,
            @Param("brandName") String brandName,
            @Param("categoryName") String categoryName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.variants v " +
           "WHERE (:query IS NULL OR :query = '' OR p.name LIKE %:query%) " +
           "AND (:brandName IS NULL OR :brandName = '' OR b.name = :brandName) " +
           "AND (:categoryName IS NULL OR :categoryName = '' OR c.name = :categoryName) " +
           "AND (v.price IS NULL OR v.price >= :minPrice) " +
           "AND (v.price IS NULL OR v.price <= :maxPrice) " +
           "ORDER BY v.price DESC")
    Page<Product> searchWithFiltersPriceDesc(
            @Param("query") String query,
            @Param("brandName") String brandName,
            @Param("categoryName") String categoryName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.variants v " +
           "WHERE (v.price IS NULL OR v.price >= :minPrice) " +
           "AND (v.price IS NULL OR v.price <= :maxPrice) " +
           "ORDER BY v.price ASC")
    Page<Product> findAllWithPriceRangeAsc(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.variants v " +
           "WHERE (v.price IS NULL OR v.price >= :minPrice) " +
           "AND (v.price IS NULL OR v.price <= :maxPrice) " +
           "ORDER BY v.price DESC")
    Page<Product> findAllWithPriceRangeDesc(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.variants v " +
           "WHERE (v.price IS NULL OR v.price >= :minPrice) " +
           "AND (v.price IS NULL OR v.price <= :maxPrice)")
    Page<Product> findAllWithPriceRange(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByNameStartingWithIgnoreCase(@Param("prefix") String prefix, Pageable pageable);
}
