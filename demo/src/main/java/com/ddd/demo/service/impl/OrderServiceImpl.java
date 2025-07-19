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
import java.math.RoundingMode;
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

    private static final Map<Order.OrderStatus, Set<Order.OrderStatus>> VALID_STATUS_TRANSITIONS = Map.of(
            Order.OrderStatus.PENDING, Set.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.CONFIRMED, Set.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.PROCESSING, Set.of(Order.OrderStatus.SHIPPED, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.SHIPPED, Set.of(Order.OrderStatus.DELIVERED),
            Order.OrderStatus.DELIVERED, Set.of(Order.OrderStatus.REFUNDED),
            Order.OrderStatus.CANCELLED, Set.of(),
            Order.OrderStatus.REFUNDED, Set.of()
    );

    private static final Set<Order.OrderStatus> CANCELLABLE_STATUSES = Set.of(
            Order.OrderStatus.PENDING,
            Order.OrderStatus.CONFIRMED,
            Order.OrderStatus.PROCESSING
    );

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        User user = getUserById(userId);
        Order order = buildOrder(user, request);

        BigDecimal subtotal = processOrderItems(order, request.getItems());
        order.setSubtotal(subtotal);

        calculateOrderAmounts(order, request);
        Order savedOrder = orderRepository.save(order);

        sendOrderConfirmationEmail(savedOrder);
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = getOrderEntityById(id);
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
        Page<Order> orders = getOrdersWithFilters(status, startDate, endDate, pageable);
        return orders.map(this::mapToOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = getOrderEntityById(id);
        validateStatusTransition(order.getStatus(), request.getStatus());

        updateOrderForNewStatus(order, request);
        Order updatedOrder = orderRepository.save(order);

        sendStatusUpdateEmail(updatedOrder);
        log.info("Order {} status updated to: {}", order.getOrderNumber(), request.getStatus());

        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id, String reason) {
        Order order = getOrderEntityById(id);
        validateOrderCancellation(order);

        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder()
                .status(Order.OrderStatus.CANCELLED)
                .cancellationReason(reason)
                .build();

        updateOrderStatus(id, request);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long id, Order.PaymentStatus status) {
        Order order = getOrderEntityById(id);
        order.setPaymentStatus(status);

        if (status == Order.PaymentStatus.PAID) {
            order.setPaymentDate(LocalDateTime.now());
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
        Order order = getOrderEntityById(id);
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
                .map(order -> order.getUser().getId().equals(userId) && isOrderCancellable(order))
                .orElse(false);
    }

    @Override
    public Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = getStartDateTime(startDate);
        LocalDateTime end = getEndDateTime(endDate);

        Map<String, Object> stats = new HashMap<>();
        populateRevenueStatistics(stats, start, end);
        populateOrderStatistics(stats);

        return stats;
    }

    @Override
    public byte[] generateInvoice(Long orderId) {
        Order order = getOrderEntityById(orderId);
        // TODO: Implement PDF generation using libraries like iText or JasperReports
        return ("Invoice for Order: " + order.getOrderNumber()).getBytes();
    }

    @Override
    @Transactional
    public void processPayment(Long orderId, String paymentToken) {
        Order order = getOrderEntityById(orderId);
        // TODO: Integrate with payment gateway (Stripe, PayPal, etc.)
        updatePaymentStatus(orderId, Order.PaymentStatus.PAID);
        log.info("Payment processed for order: {}", order.getOrderNumber());
    }

    @Override
    public BigDecimal calculateShippingFee(OrderRequest request) {
        if (request.getSubtotal() != null && request.getSubtotal().compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }

        return "EXPRESS".equals(request.getShippingMethod()) ? expressShippingFee : standardShippingFee;
    }

    @Override
    public BigDecimal applyCoupon(String couponCode, BigDecimal subtotal) {
        // TODO: Implement coupon validation and calculation
        return subtotal.multiply(new BigDecimal("0.1")); // 10% discount
    }

    // Private helper methods
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Order getOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private Order buildOrder(User user, OrderRequest request) {
        return Order.builder()
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
    }

    private BigDecimal processOrderItems(Order order, List<OrderItemRequest> itemRequests) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = getProductById(itemRequest.getProductId());
            validateStockAvailability(product, itemRequest.getQuantity());

            OrderItem orderItem = createOrderItem(order, product, itemRequest);
            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());

            productService.decreaseStock(product.getId(), itemRequest.getQuantity());
        }

        order.setOrderItems(orderItems);
        return subtotal;
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private void validateStockAvailability(Product product, Integer requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new BusinessException("Insufficient stock for product: " + product.getProductName());
        }
    }

    private OrderItem createOrderItem(Order order, Product product, OrderItemRequest request) {
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .discountAmount(BigDecimal.ZERO)
                .build();

        orderItem.calculateTotalPrice();
        return orderItem;
    }

    private void calculateOrderAmounts(Order order, OrderRequest request) {
        order.setTaxAmount(order.getSubtotal().multiply(taxRate));
        order.setShippingFee(calculateShippingFee(request));

        if (request.getCouponCode() != null) {
            BigDecimal discount = applyCoupon(request.getCouponCode(), order.getSubtotal());
            order.setDiscountAmount(discount);
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
        }

        order.calculateTotalAmount();
    }

    private void updateOrderForNewStatus(Order order, OrderStatusUpdateRequest request) {
        order.setStatus(request.getStatus());

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
                restoreStock(order);
                break;
        }
    }

    private Page<Order> getOrdersWithFilters(Order.OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null) {
            return orderRepository.findOrdersBetweenDates(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59),
                    pageable
            );
        } else if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        } else {
            return orderRepository.findAll(pageable);
        }
    }

    private LocalDateTime getStartDateTime(LocalDate startDate) {
        return startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
    }

    private LocalDateTime getEndDateTime(LocalDate endDate) {
        return endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
    }

    private void populateRevenueStatistics(Map<String, Object> stats, LocalDateTime start, LocalDateTime end) {
        List<Object[]> revenueData = orderRepository.getRevenueByDateRange(start, end);
        stats.put("revenueByDate", revenueData);

        Page<Order> orders = orderRepository.findOrdersBetweenDates(start, end, Pageable.unpaged());
        BigDecimal totalRevenue = calculateTotalRevenue(orders.getContent());

        stats.put("totalRevenue", totalRevenue);
        stats.put("totalOrders", orders.getTotalElements());
        stats.put("averageOrderValue", calculateAverageOrderValue(totalRevenue, orders.getTotalElements()));
    }

    private void populateOrderStatistics(Map<String, Object> stats) {
        List<Object[]> statusData = orderRepository.countOrdersByStatus();
        stats.put("ordersByStatus", statusData);
    }

    private BigDecimal calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAverageOrderValue(BigDecimal totalRevenue, long orderCount) {
        return orderCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
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
        if (!VALID_STATUS_TRANSITIONS.get(current).contains(next)) {
            throw new BusinessException("Invalid status transition from " + current + " to " + next);
        }
    }

    private void validateOrderCancellation(Order order) {
        if (!isOrderCancellable(order)) {
            throw new BusinessException("Order cannot be cancelled in current status: " + order.getStatus());
        }
    }

    private boolean isOrderCancellable(Order order) {
        return CANCELLABLE_STATUSES.contains(order.getStatus());
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            productService.updateStock(item.getProduct().getId(),
                    item.getProduct().getStockQuantity() + item.getQuantity());
        }
    }

    private void sendOrderConfirmationEmail(Order order) {
        Map<String, Object> variables = Map.of(
                "orderNumber", order.getOrderNumber(),
                "customerName", order.getUser().getFullName(),
                "totalAmount", order.getTotalAmount(),
                "orderItems", order.getOrderItems()
        );

        emailService.sendTemplatedEmail(
                order.getUser().getEmail(),
                "Order Confirmation - " + order.getOrderNumber(),
                "email/order-confirmation",
                variables
        );
    }

    private void sendStatusUpdateEmail(Order order) {
        Map<String, Object> variables = Map.of(
                "orderNumber", order.getOrderNumber(),
                "customerName", order.getUser().getFullName(),
                "status", order.getStatus(),
                "trackingNumber", order.getTrackingNumber()
        );

        emailService.sendTemplatedEmail(
                order.getUser().getEmail(),
                "Order Status Update - " + order.getOrderNumber(),
                "email/order-status-update",
                variables
        );
    }

    // Mapping methods
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