package com.personal.store_api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    //snapshot User
    @Column(name = "user_id")
    private String userId;

    @Column(name = "customer_name", nullable = true)
    private String customerName;

    @Column(name = "customer_phone", nullable = true)
    private String customerPhone;

    @Column(name = "customer_email", nullable = true)
    private String customerEmail;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        CANCEL,
        PENDING,
        PAID
    }
}
