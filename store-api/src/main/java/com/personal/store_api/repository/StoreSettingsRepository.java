package com.personal.store_api.repository;

import com.personal.store_api.entity.StoreSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreSettingsRepository extends JpaRepository<StoreSettings, String> {
    Optional<StoreSettings> findFirstByOrderByIdAsc();
}
