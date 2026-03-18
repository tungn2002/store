package com.personal.store_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Entity representing an invalidated (revoked) JWT token.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invalidated_tokens")
public class InvalidatedToken {
    @Id
    private String id;

    @Column(nullable = false)
    private Date expiryTime;
}
