package com.personal.store_api.repository;

import com.personal.store_api.entity.Product;
import com.personal.store_api.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    Page<ProductVariant> findByProductId(@Param("productId") Integer productId, Pageable pageable);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.size = :size AND pv.color = :color")
    Optional<ProductVariant> findByProductIdAndSizeAndColor(@Param("productId") Integer productId,
                                                             @Param("size") String size,
                                                             @Param("color") String color);

    List<ProductVariant> findByProduct(@Param("product") Product product);
}
