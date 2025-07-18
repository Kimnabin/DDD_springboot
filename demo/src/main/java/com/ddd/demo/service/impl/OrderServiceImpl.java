package com.ddd.demo.service.impl;

import com.ddd.demo.common.exception.BusinessException;
import com.ddd.demo.common.exception.ResourceNotFoundException;
import com.ddd.demo.dto.order.*;
import com.ddd.demo.dto.product.ProductResponse;
import com.ddd.demo.dto.user.UserResponse;
import com.ddd.demo.entity.order.Order;
import com.ddd.demo.entity.order.OrderItem;
import com.ddd.demo.entity.order.ShippingAddress;
import com.ddd.demo.entity.product.Product;
import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.OrderRepository;
import com.ddd.demo.repository.ProductRepository;
import com.ddd.demo.repository.UserRepository;
import com.ddd.demo.service.EmailService;
import com.ddd.demo.service.OrderService;
import com.ddd.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final EmailService emailService;

    @Value("${app.order.tax-rate:0.1}")
    private BigDecimal taxRate;

    @Value("${app.order.standard-shipping-fee:30000}")
    private BigDecimal standardShippingFee;

    @Value("${app.order.express-shipping-fee:50000}")
    private BigDecimal expressShippingFee;

    @Value("${app.order.free-shipping-threshold:500000}")
    private BigDecimal freeShippingThreshold;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .paymentMethod(Order.PaymentMethod.valueOf(request.getPaymentMethod()))
                .paymentStatus(Order.PaymentStatus.UNPAID)
                .notes(request.getNotes())
                .couponCode(request.getCouponCode())
                .shippingAddress(mapToShippingAddress(request.getShippingAddress()))
                .shippingMethod(Order.ShippingMethod.valueOf(request.getShippingMethod()))
                .build();

        // Process order items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));

            // Check stock
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getProductName());
            }

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .discountAmount(BigDecimal.ZERO)
                    .build();

            orderItem.calculateTotalPrice();
            orderItems.add(orderItem);

            subtotal = subtotal.add(orderItem.getTotalPrice());

            // Decrease stock
            productService.decreaseStock(product.getId(), itemRequest.getQuantity());
        }

        order.setOrderItems(orderItems);
        order.setSubtotal(subtotal);

        // Calculate fees
        order.setTaxAmount(subtotal.multiply(taxRate));
        order.setShippingFee(calculateShippingFee(request));

        // Apply coupon if exists
        if (request.getCouponCode() != null) {
            BigDecimal discount = applyCoupon(request.getCouponCode(), subtotal);
            order.setDiscountAmount(discount);
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
        }

        // Calculate total
        order.calculateTotalAmount();

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Send order confirmation email
        sendOrderConfirmationEmail(savedOrder);

        log.info("Order created successfully: {}", savedOrder.getOrderNumber());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getUserOrders(Long userId, Order.OrderStatus status, Pageable pageable) {
        Page<Order> orders = status == null
                ? orderRepository.findByUserId(userId, pageable)
                : orderRepository.findByUserIdAndStatus(userId, status, pageable);

        return orders.map(this::mapToOrderResponse);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Order.OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Order> orders;

        if (startDate != null && endDate != null) {
            orders = orderRepository.findOrdersBetweenDates(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59),
                    pageable
            );
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::mapToOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Validate status transition
        validateStatusTransition(order.getStatus(), request.getStatus());

        // Update status
        order.setStatus(request.getStatus());

        // Update related fields based on status
        switch (request.getStatus()) {
            case SHIPPED:
                order.setShippedDate(LocalDateTime.now());
                if (request.getTrackingNumber() != null) {
                    order.setTrackingNumber(request.getTrackingNumber());
                }
                break;
            case DELIVERED:
                order.setDeliveredDate(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledDate(LocalDateTime.now());
                order.setCancellationReason(request.getCancellationReason());
                // Restore stock
                restoreStock(order);
                break;
        }

        Order updatedOrder = orderRepository.save(order);

        // Send status update email
        sendStatusUpdateEmail(updatedOrder);

        log.info("Order {} status updated to: {}", order.getOrderNumber(), request.getStatus());

        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Check if order can be cancelled
        if (!canCancelOrder(order)) {
            throw new BusinessException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder()
                .status(Order.OrderStatus.CANCELLED)
                .cancellationReason(reason)
                .build();

        updateOrderStatus(id, request);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long id, Order.PaymentStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setPaymentStatus(status);

        if (status == Order.PaymentStatus.PAID) {
            order.setPaymentDate(LocalDateTime.now());
            // Auto confirm order when paid
            if (order.getStatus() == Order.OrderStatus.PENDING) {
                order.setStatus(Order.OrderStatus.CONFIRMED);
            }
        }

        orderRepository.save(order);

        log.info("Order {} payment status updated to: {}", order.getOrderNumber(), status);
    }

    @Override
    @Transactional
    public void updateShippingInfo(Long id, String trackingNumber) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setTrackingNumber(trackingNumber);

        if (order.getStatus() == Order.OrderStatus.PROCESSING) {
            order.setStatus(Order.OrderStatus.SHIPPED);
            order.setShippedDate(LocalDateTime.now());
        }

        orderRepository.save(order);

        log.info("Order {} tracking number updated: {}", order.getOrderNumber(), trackingNumber);
    }

    @Override
    public boolean isOrderOwner(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getId().equals(userId))
                .orElse(false);
    }

    @Override
    public boolean canCancelOrder(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getId().equals(userId) && canCancelOrder(order))
                .orElse(false);
    }

    @Override
    public Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        // Set date range
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        // Get revenue by date
        List<Object[]> revenueData = orderRepository.getRevenueByDateRange(start, end);
        stats.put("revenueByDate", revenueData);

        // Get order count by status
        List<Object[]> statusData = orderRepository.countOrdersByStatus();
        stats.put("ordersByStatus", statusData);

        // Calculate totals
        Page<Order> orders = orderRepository.findOrdersBetweenDates(start, end, Pageable.unpaged());
        BigDecimal totalRevenue = orders.getContent().stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalRevenue", totalRevenue);
        stats.put("totalOrders", orders.getTotalElements());
        stats.put("averageOrderValue", orders.getTotalElements() > 0
                ? totalRevenue.divide(BigDecimal.valueOf(orders.getTotalElements()), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO);

        return stats;
    }

    @Override
    public byte[] generateInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // TODO: Implement PDF generation using libraries like iText or JasperReports
        // For now, return a placeholder
        return ("Invoice for Order: " + order.getOrderNumber()).getBytes();
    }

    @Override
    @Transactional
    public void processPayment(Long orderId, String paymentToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // TODO: Integrate with payment gateway (Stripe, PayPal, etc.)
        // For now, just update payment status

        updatePaymentStatus(orderId, Order.PaymentStatus.PAID);

        log.info("Payment processed for order: {}", order.getOrderNumber());
    }

    @Override
    public BigDecimal calculateShippingFee(OrderRequest request) {
        // Free shipping for orders above threshold
        if (request.getSubtotal() != null && request.getSubtotal().compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }

        // Calculate based on shipping method
        return request.getShippingMethod().equals("EXPRESS") ? expressShippingFee : standardShippingFee;
    }

    @Override
    public BigDecimal applyCoupon(String couponCode, BigDecimal subtotal) {
        // TODO: Implement coupon validation and calculation
        // For now, return a fixed discount
        return subtotal.multiply(new BigDecimal("0.1")); // 10% discount
    }

    // Helper methods
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private ShippingAddress mapToShippingAddress(ShippingAddressRequest request) {
        return ShippingAddress.builder()
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getPhoneNumber())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .province(request.getProvince())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();
    }

    private void validateStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
        // Define valid transitions
        Map<Order.OrderStatus, Set<Order.OrderStatus>> validTransitions = Map.of(
                Order.OrderStatus.PENDING, Set.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.CANCELLED),
                Order.OrderStatus.CONFIRMED, Set.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.CANCELLED),
                Order.OrderStatus.PROCESSING, Set.of(Order.OrderStatus.SHIPPED, Order.OrderStatus.CANCELLED),
                Order.OrderStatus.SHIPPED, Set.of(Order.OrderStatus.DELIVERED),
                Order.OrderStatus.DELIVERED, Set.of(Order.OrderStatus.REFUNDED),
                Order.OrderStatus.CANCELLED, Set.of(),
                Order.OrderStatus.REFUNDED, Set.of()
        );

        if (!validTransitions.get(current).contains(next)) {
            throw new BusinessException("Invalid status transition from " + current + " to " + next);
        }
    }

    private boolean canCancelOrder(Order order) {
        return Set.of(Order.OrderStatus.PENDING, Order.OrderStatus.CONFIRMED, Order.OrderStatus.PROCESSING)
                .contains(order.getStatus());
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            productService.updateStock(item.getProduct().getId(),
                    item.getProduct().getStockQuantity() + item.getQuantity());
        }
    }

    private void sendOrderConfirmationEmail(Order order) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNumber", order.getOrderNumber());
        variables.put("customerName", order.getUser().getFullName());
        variables.put("totalAmount", order.getTotalAmount());
        variables.put("orderItems", order.getOrderItems());

        emailService.sendTemplatedEmail(
                order.getUser().getEmail(),
                "Order Confirmation - " + order.getOrderNumber(),
                "email/order-confirmation",
                variables
        );
    }

    private void sendStatusUpdateEmail(Order order) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNumber", order.getOrderNumber());
        variables.put("customerName", order.getUser().getFullName());
        variables.put("status", order.getStatus());
        variables.put("trackingNumber", order.getTrackingNumber());

        emailService.sendTemplatedEmail(
                order.getUser().getEmail(),
                "Order Status Update - " + order.getOrderNumber(),
                "email/order-status-update",
                variables
        );
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .user(mapToUserResponse(order.getUser()))
                .orderItems(order.getOrderItems().stream()
                        .map(this::mapToOrderItemResponse)
                        .collect(Collectors.toList()))
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .paymentDate(order.getPaymentDate())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .shippingMethod(order.getShippingMethod())
                .trackingNumber(order.getTrackingNumber())
                .shippedDate(order.getShippedDate())
                .deliveredDate(order.getDeliveredDate())
                .notes(order.getNotes())
                .couponCode(order.getCouponCode())
                .cancelledDate(order.getCancelledDate())
                .cancellationReason(order.getCancellationReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .product(mapToProductResponse(item.getProduct()))
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .totalPrice(item.getTotalPrice())
                .notes(item.getNotes())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .sku(product.getSku())
                .category(product.getCategory())
                .build();
    }
}