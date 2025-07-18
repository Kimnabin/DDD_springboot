package com.ddd.demo.entity.product;

import com.ddd.demo.entity.base.BaseEntity;
import com.ddd.demo.entity.order.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "productName"),
        @Index(name = "idx_product_price", columnList = "price"),
        @Index(name = "idx_product_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(length = 50)
    private String sku; // Stock Keeping Unit

    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    // One product can be in many order items
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<OrderItem> orderItems = new HashSet<>();

    public enum ProductStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK
    }
}