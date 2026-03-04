package com.personal.store_api.repository;

import com.personal.store_api.entity.Cart;
import com.personal.store_api.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    @EntityGraph(attributePaths = {"productVariant", "productVariant.product"})
    Optional<Cart> findByUserIdAndProductVariantId(@Param("userId") String userId, @Param("variantId") Integer variantId);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user = :user")
    int countByUser(@Param("user") User user);

}
