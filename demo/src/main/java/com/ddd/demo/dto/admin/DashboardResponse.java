package com.ddd.demo.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // Overview statistics
    private OverviewStats overview;

    // Revenue statistics
    private RevenueStats revenue;

    // Order statistics
    private OrderStats orders;

    // User statistics
    private UserStats users;

    // Product statistics
    private ProductStats products;

    // Recent activities
    private List<RecentActivity> recentActivities;

    // Charts data
    private ChartData chartData;

    @Data
    @Builder
    public static class OverviewStats {
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private Long totalUsers;
        private Long totalProducts;
        private BigDecimal revenueGrowth; // Percentage
        private BigDecimal orderGrowth;   // Percentage
        private BigDecimal userGrowth;    // Percentage
    }

    @Data
    @Builder
    public static class RevenueStats {
        private BigDecimal todayRevenue;
        private BigDecimal weekRevenue;
        private BigDecimal monthRevenue;
        private BigDecimal yearRevenue;
        private Map<String, BigDecimal> revenueByCategory;
        private Map<String, BigDecimal> revenueByPaymentMethod;
    }

    @Data
    @Builder
    public static class OrderStats {
        private Long pendingOrders;
        private Long processingOrders;
        private Long shippedOrders;
        private Long deliveredOrders;
        private Long cancelledOrders;
        private BigDecimal averageOrderValue;
        private Map<String, Long> ordersByStatus;
    }

    @Data
    @Builder
    public static class UserStats {
        private Long activeUsers;
        private Long newUsersToday;
        private Long newUsersThisWeek;
        private Long newUsersThisMonth;
        private Map<String, Long> usersByRole;
    }

    @Data
    @Builder
    public static class ProductStats {
        private Long activeProducts;
        private Long outOfStockProducts;
        private Long lowStockProducts;
        private List<TopProduct> topSellingProducts;
        private Map<String, Long> productsByCategory;
    }

    @Data
    @Builder
    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long soldQuantity;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    public static class RecentActivity {
        private String type; // ORDER, USER, PRODUCT
        private String action; // CREATED, UPDATED, DELETED
        private String description;
        private String performedBy;
        private LocalDate timestamp;
    }

    @Data
    @Builder
    public static class ChartData {
        private List<DailyRevenue> dailyRevenue;
        private List<CategorySales> categorySales;
        private List<UserGrowth> userGrowth;
    }

    @Data
    @Builder
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Long orderCount;
    }

    @Data
    @Builder
    public static class CategorySales {
        private String category;
        private Long quantity;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    public static class UserGrowth {
        private LocalDate date;
        private Long newUsers;
        private Long totalUsers;
    }
}