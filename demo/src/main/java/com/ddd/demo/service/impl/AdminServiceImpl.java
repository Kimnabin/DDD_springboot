package com.ddd.demo.service.impl;

import com.ddd.demo.dto.admin.*;
import com.ddd.demo.entity.order.Order;
import com.ddd.demo.entity.product.Product;
import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.OrderRepository;
import com.ddd.demo.repository.ProductRepository;
import com.ddd.demo.repository.UserRepository;
import com.ddd.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    public DashboardResponse getDashboardData(LocalDate startDate, LocalDate endDate) {
        // Set default date range if not provided
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        // Build dashboard response
        return DashboardResponse.builder()
                .overview(getOverviewStats(start, end))
                .revenue(getRevenueStats(start, end))
                .orders(getOrderStats())
                .users(getUserStats())
                .products(getProductStats())
                .recentActivities(getRecentActivities())
                .chartData(getChartData(start, end))
                .build();
    }

    @Override
    public SystemInfoResponse getSystemInfo() {
        return SystemInfoResponse.builder()
                .application(getApplicationInfo())
                .server(getServerInfo())
                .database(getDatabaseInfo())
                .performance(getPerformanceMetrics())
                .build();
    }

    @Override
    @Transactional
    public void updateSystemConfig(SystemConfigRequest request) {
        // Store configuration in Redis or database
        String configKey = "system:config";
        redisTemplate.opsForValue().set(configKey, request);

        // Clear relevant caches
        clearCache("config");

        log.info("System configuration updated");
    }

    @Override
    public byte[] exportData(String type, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(type);

            switch (type.toLowerCase()) {
                case "orders":
                    exportOrders(sheet, startDate, endDate);
                    break;
                case "users":
                    exportUsers(sheet);
                    break;
                case "products":
                    exportProducts(sheet);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid export type: " + type);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting data", e);
            throw new RuntimeException("Failed to export data", e);
        }
    }

    @Override
    public void backupDatabase() {
        // TODO: Implement database backup logic
        log.info("Database backup initiated");
    }

    @Override
    public void clearCache(String cacheName) {
        if (cacheName == null || cacheName.isEmpty()) {
            // Clear all caches
            cacheManager.getCacheNames().forEach(name -> {
                Cache cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            });
            log.info("All caches cleared");
        } else {
            // Clear specific cache
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cache cleared: {}", cacheName);
            }
        }
    }

    @Override
    public Page<AuditLogResponse> getAuditLogs(String entity, String action, Pageable pageable) {
        // TODO: Implement audit log retrieval
        return Page.empty();
    }

    @Override
    public void sendSystemNotification(String message, String type) {
        // TODO: Implement system notification logic
        log.info("System notification sent: {} - {}", type, message);
    }

    @Override
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        // Check database connection
        health.put("database", checkDatabaseHealth());

        // Check Redis connection
        health.put("redis", checkRedisHealth());

        // Check disk space
        health.put("disk", checkDiskSpace());

        // Check memory
        health.put("memory", checkMemoryHealth());

        // Overall status
        boolean allHealthy = health.values().stream()
                .allMatch(status -> "UP".equals(((Map<?, ?>) status).get("status")));
        health.put("status", allHealthy ? "UP" : "DOWN");

        return health;
    }

    // Helper methods for dashboard data
    private DashboardResponse.OverviewStats getOverviewStats(LocalDateTime start, LocalDateTime end) {
        // Calculate current period stats
        List<Order> currentOrders = orderRepository.findOrdersBetweenDates(start, end, Pageable.unpaged()).getContent();
        BigDecimal currentRevenue = currentOrders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate previous period stats for growth comparison
        LocalDateTime prevStart = start.minusDays(end.toLocalDate().toEpochDay() - start.toLocalDate().toEpochDay());
        LocalDateTime prevEnd = start.minusSeconds(1);
        List<Order> prevOrders = orderRepository.findOrdersBetweenDates(prevStart, prevEnd, Pageable.unpaged()).getContent();
        BigDecimal prevRevenue = prevOrders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate growth percentages
        BigDecimal revenueGrowth = calculateGrowth(prevRevenue, currentRevenue);
        BigDecimal orderGrowth = calculateGrowth(
                BigDecimal.valueOf(prevOrders.size()),
                BigDecimal.valueOf(currentOrders.size())
        );

        return DashboardResponse.OverviewStats.builder()
                .totalRevenue(currentRevenue)
                .totalOrders((long) currentOrders.size())
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .revenueGrowth(revenueGrowth)
                .orderGrowth(orderGrowth)
                .userGrowth(BigDecimal.ZERO) // TODO: Calculate user growth
                .build();
    }

    private DashboardResponse.RevenueStats getRevenueStats(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        // Calculate revenue for different periods
        BigDecimal todayRevenue = calculateRevenueBetween(now.toLocalDate().atStartOfDay(), now);
        BigDecimal weekRevenue = calculateRevenueBetween(now.minusWeeks(1), now);
        BigDecimal monthRevenue = calculateRevenueBetween(now.minusMonths(1), now);
        BigDecimal yearRevenue = calculateRevenueBetween(now.minusYears(1), now);

        // Revenue by category
        Map<String, BigDecimal> revenueByCategory = orderRepository.findOrdersBetweenDates(start, end, Pageable.unpaged())
                .getContent().stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory(),
                        Collectors.reducing(BigDecimal.ZERO,
                                OrderItem::getTotalPrice,
                                BigDecimal::add)
                ));

        // Revenue by payment method
        Map<String, BigDecimal> revenueByPaymentMethod = orderRepository.findOrdersBetweenDates(start, end, Pageable.unpaged())
                .getContent().stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .collect(Collectors.groupingBy(
                        o -> o.getPaymentMethod().toString(),
                        Collectors.reducing(BigDecimal.ZERO,
                                Order::getTotalAmount,
                                BigDecimal::add)
                ));

        return DashboardResponse.RevenueStats.builder()
                .todayRevenue(todayRevenue)
                .weekRevenue(weekRevenue)
                .monthRevenue(monthRevenue)
                .yearRevenue(yearRevenue)
                .revenueByCategory(revenueByCategory)
                .revenueByPaymentMethod(revenueByPaymentMethod)
                .build();
    }

    private DashboardResponse.OrderStats getOrderStats() {
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        Map<String, Long> ordersByStatus = new HashMap<>();

        for (Object[] row : statusCounts) {
            Order.OrderStatus status = (Order.OrderStatus) row[0];
            Long count = (Long) row[1];
            ordersByStatus.put(status.toString(), count);
        }

        // Calculate average order value
        List<Order> paidOrders = orderRepository.findAll().stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = paidOrders.isEmpty() ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(paidOrders.size()), 2, BigDecimal.ROUND_HALF_UP);

        return DashboardResponse.OrderStats.builder()
                .pendingOrders(ordersByStatus.getOrDefault("PENDING", 0L))
                .processingOrders(ordersByStatus.getOrDefault("PROCESSING", 0L))
                .shippedOrders(ordersByStatus.getOrDefault("SHIPPED", 0L))
                .deliveredOrders(ordersByStatus.getOrDefault("DELIVERED", 0L))
                .cancelledOrders(ordersByStatus.getOrDefault("CANCELLED", 0L))
                .averageOrderValue(averageOrderValue)
                .ordersByStatus(ordersByStatus)
                .build();
    }

    private DashboardResponse.UserStats getUserStats() {
        LocalDateTime now = LocalDateTime.now();

        // Count active users
        long activeUsers = userRepository.findByIsActiveTrue(Pageable.unpaged()).getTotalElements();

        // Count new users
        long newUsersToday = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().toLocalDate().equals(now.toLocalDate()))
                .count();

        long newUsersThisWeek = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(now.minusWeeks(1)))
                .count();

        long newUsersThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(now.minusMonths(1)))
                .count();

        // Users by role
        Map<String, Long> usersByRole = userRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRole().toString(),
                        Collectors.counting()
                ));

        return DashboardResponse.UserStats.builder()
                .activeUsers(activeUsers)
                .newUsersToday(newUsersToday)
                .newUsersThisWeek(newUsersThisWeek)
                .newUsersThisMonth(newUsersThisMonth)
                .usersByRole(usersByRole)
                .build();
    }

    private DashboardResponse.ProductStats getProductStats() {
        // Count products by status
        long activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == Product.ProductStatus.ACTIVE)
                .count();

        long outOfStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == Product.ProductStatus.OUT_OF_STOCK)
                .count();

        // Get low stock products
        List<Product> lowStockProducts = productRepository.findLowStockProducts(10);

        // Get top selling products
        List<DashboardResponse.TopProduct> topSellingProducts = getTopSellingProducts(5);

        // Products by category
        Map<String, Long> productsByCategory = productRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.counting()
                ));

        return DashboardResponse.ProductStats.builder()
                .activeProducts(activeProducts)
                .outOfStockProducts(outOfStockProducts)
                .lowStockProducts((long) lowStockProducts.size())
                .topSellingProducts(topSellingProducts)
                .productsByCategory(productsByCategory)
                .build();
    }

    private List<DashboardResponse.RecentActivity> getRecentActivities() {
        // TODO: Implement activity tracking
        return new ArrayList<>();
    }

    private DashboardResponse.ChartData getChartData(LocalDateTime start, LocalDateTime end) {
        // Get daily revenue
        List<DashboardResponse.DailyRevenue> dailyRevenue = getDailyRevenue(start, end);

        // Get category sales
        List<DashboardResponse.CategorySales> categorySales = getCategorySales(start, end);

        // Get user growth
        List<DashboardResponse.UserGrowth> userGrowth = getUserGrowth(start, end);

        return DashboardResponse.ChartData.builder()
                .dailyRevenue(dailyRevenue)
                .categorySales(categorySales)
                .userGrowth(userGrowth)
                .build();
    }

    // Helper methods for system info
    private SystemInfoResponse.ApplicationInfo getApplicationInfo() {
        return SystemInfoResponse.ApplicationInfo.builder()
                .name(applicationName)
                .version(applicationVersion)
                .environment(activeProfile)
                .startupTime(LocalDateTime.now()) // TODO: Track actual startup time
                .uptimeHours(0L) // TODO: Calculate actual uptime
                .features(getEnabledFeatures())
                .build();
    }

    private SystemInfoResponse.ServerInfo getServerInfo() {
        return SystemInfoResponse.ServerInfo.builder()
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .javaVersion(System.getProperty("java.version"))
                .serverPort("8080") // TODO: Get from configuration
                .timezone(TimeZone.getDefault().getID())
                .availableProcessors(Runtime.getRuntime().availableProcessors())
                .build();
    }

    private SystemInfoResponse.DatabaseInfo getDatabaseInfo() {
        return SystemInfoResponse.DatabaseInfo.builder()
                .databaseName("MySQL") // TODO: Get from configuration
                .databaseVersion("8.0") // TODO: Get actual version
                .driverVersion("8.0.33") // TODO: Get actual version
                .connectionPoolSize(10L) // TODO: Get from configuration
                .activeConnections(5L) // TODO: Get actual count
                .databaseSize("100MB") // TODO: Calculate actual size
                .build();
    }

    private SystemInfoResponse.PerformanceMetrics getPerformanceMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        // Memory info
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = Runtime.getRuntime().maxMemory();
        double memoryUsage = (double) usedMemory / totalMemory * 100;

        SystemInfoResponse.MemoryInfo memoryInfo = SystemInfoResponse.MemoryInfo.builder()
                .totalMemory(totalMemory)
                .usedMemory(usedMemory)
                .freeMemory(freeMemory)
                .maxMemory(maxMemory)
                .memoryUsagePercentage(memoryUsage)
                .build();

        // CPU info
        SystemInfoResponse.CpuInfo cpuInfo = SystemInfoResponse.CpuInfo.builder()
                .cpuUsage(osBean.getProcessCpuLoad() * 100)
                .systemLoadAverage(osBean.getSystemLoadAverage())
                .threadCount(Thread.activeCount())
                .build();

        // Disk info
        File root = new File("/");
        SystemInfoResponse.DiskInfo diskInfo = SystemInfoResponse.DiskInfo.builder()
                .totalSpace(root.getTotalSpace())
                .freeSpace(root.getFreeSpace())
                .usedSpace(root.getTotalSpace() - root.getFreeSpace())
                .diskUsagePercentage((double) (root.getTotalSpace() - root.getFreeSpace()) / root.getTotalSpace() * 100)
                .build();

        return SystemInfoResponse.PerformanceMetrics.builder()
                .memory(memoryInfo)
                .cpu(cpuInfo)
                .disk(diskInfo)
                .totalRequests(0L) // TODO: Track actual requests
                .averageResponseTime(0.0) // TODO: Track actual response time
                .errorCount(0L) // TODO: Track actual errors
                .build();
    }

    // Export helper methods
    private void exportOrders(Sheet sheet, LocalDate startDate, LocalDate endDate) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Number", "Customer", "Total Amount", "Status", "Payment Status", "Created Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Get orders
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusYears(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
        List<Order> orders = orderRepository.findOrdersBetweenDates(start, end, Pageable.unpaged()).getContent();

        // Add data rows
        int rowNum = 1;
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getOrderNumber());
            row.createCell(1).setCellValue(order.getUser().getFullName());
            row.createCell(2).setCellValue(order.getTotalAmount().doubleValue());
            row.createCell(3).setCellValue(order.getStatus().toString());
            row.createCell(4).setCellValue(order.getPaymentStatus().toString());
            row.createCell(5).setCellValue(order.getCreatedAt().toString());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void exportUsers(Sheet sheet) {
        // Similar implementation for users
    }

    private void exportProducts(Sheet sheet) {
        // Similar implementation for products
    }

    // Utility methods
    private BigDecimal calculateGrowth(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findOrdersBetweenDates(start, end, Pageable.unpaged())
                .getContent().stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<DashboardResponse.TopProduct> getTopSellingProducts(int limit) {
        // TODO: Implement actual top selling products query
        return new ArrayList<>();
    }

    private List<DashboardResponse.DailyRevenue> getDailyRevenue(LocalDateTime start, LocalDateTime end) {
        // TODO: Implement daily revenue calculation
        return new ArrayList<>();
    }

    private List<DashboardResponse.CategorySales> getCategorySales(LocalDateTime start, LocalDateTime end) {
        // TODO: Implement category sales calculation
        return new ArrayList<>();
    }

    private List<DashboardResponse.UserGrowth> getUserGrowth(LocalDateTime start, LocalDateTime end) {
        // TODO: Implement user growth calculation
        return new ArrayList<>();
    }

    private Map<String, String> getEnabledFeatures() {
        Map<String, String> features = new HashMap<>();
        features.put("twoFactorAuth", "enabled");
        features.put("emailNotifications", "enabled");
        features.put("exportData", "enabled");
        return features;
    }

    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        try {
            userRepository.count();
            health.put("status", "UP");
            health.put("message", "Database connection is healthy");
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("message", "Database connection failed: " + e.getMessage());
        }
        return health;
    }

    private Map<String, Object> checkRedisHealth() {
        Map<String, Object> health = new HashMap<>();
        try {
            redisTemplate.opsForValue().set("health:check", "OK");
            health.put("status", "UP");
            health.put("message", "Redis connection is healthy");
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("message", "Redis connection failed: " + e.getMessage());
        }
        return health;
    }

    private Map<String, Object> checkDiskSpace() {
        Map<String, Object> health = new HashMap<>();
        File root = new File("/");
        double usagePercentage = (double) (root.getTotalSpace() - root.getFreeSpace()) / root.getTotalSpace() * 100;

        if (usagePercentage < 80) {
            health.put("status", "UP");
            health.put("message", String.format("Disk usage: %.2f%%", usagePercentage));
        } else {
            health.put("status", "WARNING");
            health.put("message", String.format("High disk usage: %.2f%%", usagePercentage));
        }
        return health;
    }

    private Map<String, Object> checkMemoryHealth() {
        Map<String, Object> health = new HashMap<>();
        double memoryUsage = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                / Runtime.getRuntime().totalMemory() * 100;

        if (memoryUsage < 80) {
            health.put("status", "UP");
            health.put("message", String.format("Memory usage: %.2f%%", memoryUsage));
        } else {
            health.put("status", "WARNING");
            health.put("message", String.format("High memory usage: %.2f%%", memoryUsage));
        }
        return health;
    }
}