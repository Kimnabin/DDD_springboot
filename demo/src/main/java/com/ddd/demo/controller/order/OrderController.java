package com.ddd.demo.controller.order;

import com.ddd.demo.common.response.ApiResponse;
import com.ddd.demo.common.response.PageResponse;
import com.ddd.demo.dto.order.OrderRequest;
import com.ddd.demo.dto.order.OrderResponse;
import com.ddd.demo.dto.order.OrderStatusUpdateRequest;
import com.ddd.demo.entity.order.Order;
import com.ddd.demo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    // Create order
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create order", description = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order created successfully"));
    }

    // Get order by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    @Operation(summary = "Get order", description = "Get order details by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    // Get order by order number
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get by order number", description = "Get order by order number")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    // Get user orders
    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my orders", description = "Get current user's orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Order.OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> orders = orderService.getUserOrders(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(orders)));
    }

    // Get all orders - Admin
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders", description = "Get all orders (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> orders = orderService.getAllOrders(status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(orders)));
    }

    // Update order status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update status", description = "Update order status (Admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated"));
    }

    // Cancel order
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @orderService.canCancelOrder(#id, authentication.principal.id)")
    @Operation(summary = "Cancel order", description = "Cancel an order")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled successfully"));
    }

    // Update payment status
    @PatchMapping("/{id}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update payment", description = "Update payment status")
    public ResponseEntity<ApiResponse<Void>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam Order.PaymentStatus status) {
        orderService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment status updated"));
    }

    // Update shipping info
    @PatchMapping("/{id}/shipping")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update shipping", description = "Update shipping information")
    public ResponseEntity<ApiResponse<Void>> updateShippingInfo(
            @PathVariable Long id,
            @RequestParam String trackingNumber) {
        orderService.updateShippingInfo(id, trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(null, "Shipping info updated"));
    }

    // Get order statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics", description = "Get order statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = orderService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // Generate invoice
    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    @Operation(summary = "Get invoice", description = "Generate order invoice")
    public ResponseEntity<byte[]> generateInvoice(@PathVariable Long id) {
        byte[] invoice = orderService.generateInvoice(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=invoice-" + id + ".pdf")
                .body(invoice);
    }
}