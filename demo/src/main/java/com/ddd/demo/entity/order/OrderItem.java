package com.ddd.demo.entity.order;

import com.ddd.demo.entity.base.BaseEntity;
import com.ddd.demo.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    // Many items belong to one order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Many items can reference one product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // Price at the time of order (to preserve historical pricing)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Discount for this specific item
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Total price for this line item
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Additional info
    @Column(length = 500)
    private String notes;

    // Calculate total price for this item
    public void calculateTotalPrice() {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.totalPrice = subtotal.subtract(discountAmount);
    }
}