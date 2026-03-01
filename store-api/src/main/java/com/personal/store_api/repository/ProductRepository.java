package com.personal.store_api.repository;

import com.personal.store_api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
