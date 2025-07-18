package com.ddd.demo.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfoResponse {

    // Application info
    private ApplicationInfo application;

    // Server info
    private ServerInfo server;

    // Database info
    private DatabaseInfo database;

    // Performance metrics
    private PerformanceMetrics performance;

    @Data
    @Builder
    public static class ApplicationInfo {
        private String name;
        private String version;
        private String environment;
        private LocalDateTime startupTime;
        private Long uptimeHours;
        private Map<String, String> features;
    }

    @Data
    @Builder
    public static class ServerInfo {
        private String osName;
        private String osVersion;
        private String javaVersion;
        private String serverPort;
        private String timezone;
        private Integer availableProcessors;
    }

    @Data
    @Builder
    public static class DatabaseInfo {
        private String databaseName;
        private String databaseVersion;
        private String driverVersion;
        private Long connectionPoolSize;
        private Long activeConnections;
        private String databaseSize;
    }

    @Data
    @Builder
    public static class PerformanceMetrics {
        private MemoryInfo memory;
        private CpuInfo cpu;
        private DiskInfo disk;
        private Long totalRequests;
        private Double averageResponseTime;
        private Long errorCount;
    }

    @Data
    @Builder
    public static class MemoryInfo {
        private Long totalMemory;
        private Long usedMemory;
        private Long freeMemory;
        private Long maxMemory;
        private Double memoryUsagePercentage;
    }

    @Data
    @Builder
    public static class CpuInfo {
        private Double cpuUsage;
        private Double systemLoadAverage;
        private Integer threadCount;
    }

    @Data
    @Builder
    public static class DiskInfo {
        private Long totalSpace;
        private Long usedSpace;
        private Long freeSpace;
        private Double diskUsagePercentage;
    }
}