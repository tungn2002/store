package com.personal.store_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * Entity representing a role (collection of permissions).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    private String displayName;

    @ManyToMany
    private Set<Permission> permissions;
}
