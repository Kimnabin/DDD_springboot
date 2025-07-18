package com.ddd.demo.repository;

import com.ddd.demo.entity.order.Order;
import com.ddd.demo.entity.order.Order.OrderStatus;
import com.ddd.demo.entity.order.Order.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find orders by user
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // Find orders by status
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Find orders by user and status
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    // Find orders created between dates
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    // Find unpaid orders older than X days
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'UNPAID' AND o.createdAt < :date")
    List<Order> findUnpaidOrdersOlderThan(@Param("date") LocalDateTime date);

    // Update order status
    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") OrderStatus status);

    // Update payment status
    @Modifying
    @Query("UPDATE Order o SET o.paymentStatus = :paymentStatus, o.paymentDate = :paymentDate WHERE o.id = :orderId")
    int updatePaymentStatus(@Param("orderId") Long orderId,
                            @Param("paymentStatus") PaymentStatus paymentStatus,
                            @Param("paymentDate") LocalDateTime paymentDate);

    // Get order statistics by user
    @Query("SELECT COUNT(o), SUM(o.totalAmount), AVG(o.totalAmount) FROM Order o WHERE o.user.id = :userId AND o.status != 'CANCELLED'")
    List<Object[]> getOrderStatsByUser(@Param("userId") Long userId);

    // Get revenue by date range
    @Query("SELECT DATE(o.createdAt), SUM(o.totalAmount) FROM Order o " +
            "WHERE o.paymentStatus = 'PAID' AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(o.createdAt)")
    List<Object[]> getRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Find orders with total amount greater than
    @Query("SELECT o FROM Order o WHERE o.totalAmount > :amount ORDER BY o.totalAmount DESC")
    List<Order> findHighValueOrders(@Param("amount") BigDecimal amount);

    // Count orders by status
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    // Find orders to be delivered today
    @Query("SELECT o FROM Order o WHERE o.status = 'SHIPPED' AND DATE(o.shippedDate) = CURRENT_DATE")
    List<Order> findOrdersToBeDeliveredToday();
}