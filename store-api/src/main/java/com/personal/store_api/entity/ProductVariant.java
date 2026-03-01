package com.personal.store_api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_variants", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "size", "color"}))
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 10)
    private String size;

    @Column(length = 30)
    private String color;

    private String image;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Integer stockQuantity;
}
