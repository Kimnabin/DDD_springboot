package com.ddd.demo.service;

import com.ddd.demo.dto.order.OrderRequest;
import com.ddd.demo.dto.order.OrderResponse;
import com.ddd.demo.dto.order.OrderStatusUpdateRequest;
import com.ddd.demo.entity.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface OrderService {

    // Create new order
    OrderResponse createOrder(Long userId, OrderRequest request);

    // Get order by ID
    OrderResponse getOrderById(Long id);

    // Get order by order number
    OrderResponse getOrderByNumber(String orderNumber);

    // Get user orders
    Page<OrderResponse> getUserOrders(Long userId, Order.OrderStatus status, Pageable pageable);

    // Get all orders (Admin)
    Page<OrderResponse> getAllOrders(Order.OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Update order status
    OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request);

    // Cancel order
    void cancelOrder(Long id, String reason);

    // Update payment status
    void updatePaymentStatus(Long id, Order.PaymentStatus status);

    // Update shipping info
    void updateShippingInfo(Long id, String trackingNumber);

    // Check if user owns order
    boolean isOrderOwner(Long orderId, Long userId);

    // Check if order can be cancelled
    boolean canCancelOrder(Long orderId, Long userId);

    // Get order statistics
    Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate);

    // Generate invoice
    byte[] generateInvoice(Long orderId);

    // Process payment
    void processPayment(Long orderId, String paymentToken);

    // Calculate shipping fee
    BigDecimal calculateShippingFee(OrderRequest request);

    // Apply coupon
    BigDecimal applyCoupon(String couponCode, BigDecimal subtotal);
}