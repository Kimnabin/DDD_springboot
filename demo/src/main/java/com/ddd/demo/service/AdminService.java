package com.ddd.demo.service;

import com.ddd.demo.dto.admin.AuditLogResponse;
import com.ddd.demo.dto.admin.DashboardResponse;
import com.ddd.demo.dto.admin.SystemConfigRequest;
import com.ddd.demo.dto.admin.SystemInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface AdminService {

    // Get dashboard data
    DashboardResponse getDashboardData(LocalDate startDate, LocalDate endDate);

    // Get system information
    SystemInfoResponse getSystemInfo();

    // Update system configuration
    void updateSystemConfig(SystemConfigRequest request);

    // Export data
    byte[] exportData(String type, LocalDate startDate, LocalDate endDate);

    // Backup database
    void backupDatabase();

    // Clear cache
    void clearCache(String cacheName);

    // Get audit logs
    Page<AuditLogResponse> getAuditLogs(String entity, String action, Pageable pageable);

    // Send system notification
    void sendSystemNotification(String message, String type);

    // Get system health
    Map<String, Object> getSystemHealth();
}