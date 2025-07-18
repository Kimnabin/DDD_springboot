package com.ddd.demo.dto.order;

import com.ddd.demo.dto.user.UserResponse;
import com.ddd.demo.entity.order.Order;
import com.ddd.demo.entity.order.ShippingAddress;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private UserResponse user;
    private List<OrderItemResponse> orderItems;
    private Order.OrderStatus status;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private ShippingAddress shippingAddress;
    private Order.ShippingMethod shippingMethod;
    private String trackingNumber;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private String notes;
    private String couponCode;
    private LocalDateTime cancelledDate;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}