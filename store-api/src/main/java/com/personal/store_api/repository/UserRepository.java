package com.personal.store_api.repository;

import com.personal.store_api.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    @EntityGraph(attributePaths = {
            "roles",
            "roles.permissions"
    })
    Optional<User> findByEmail(String email);

}
