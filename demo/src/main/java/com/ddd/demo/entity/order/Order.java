package com.ddd.demo.entity.order;

import com.ddd.demo.entity.base.BaseEntity;
import com.ddd.demo.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "orderNumber", unique = true),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String orderNumber;

    // Many orders belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Order items relationship
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // Order status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // Payment information
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private LocalDateTime paymentDate;

    // Pricing
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Shipping information
    @Embedded
    private ShippingAddress shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ShippingMethod shippingMethod;

    private String trackingNumber;

    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;

    // Additional information
    @Column(length = 500)
    private String notes;

    @Column(length = 50)
    private String couponCode;

    private LocalDateTime cancelledDate;

    @Column(length = 500)
    private String cancellationReason;

    // Helper methods
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    // Calculate total amount
    public void calculateTotalAmount() {
        this.totalAmount = subtotal
                .add(taxAmount)
                .add(shippingFee)
                .subtract(discountAmount);
    }

    // Enums
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }

    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        CREDIT_CARD,
        DEBIT_CARD,
        BANK_TRANSFER,
        E_WALLET,
        PAYPAL
    }

    public enum PaymentStatus {
        UNPAID,
        PAID,
        REFUNDED,
        FAILED
    }

    public enum ShippingMethod {
        STANDARD,
        EXPRESS,
        OVERNIGHT,
        PICKUP
    }
}