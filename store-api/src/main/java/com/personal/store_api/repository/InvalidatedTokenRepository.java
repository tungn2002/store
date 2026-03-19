package com.personal.store_api.repository;

import com.personal.store_api.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    @Modifying
    @Query("DELETE FROM InvalidatedToken t WHERE t.expiryTime < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
