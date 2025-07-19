package com.ddd.demo.controller.admin;

import com.ddd.demo.common.response.ApiResponse;
import com.ddd.demo.dto.admin.DashboardResponse;
import com.ddd.demo.dto.admin.SystemConfigRequest;
import com.ddd.demo.dto.admin.SystemInfoResponse;
import com.ddd.demo.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Management", description = "Admin management APIs")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard", description = "Get admin dashboard data")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        DashboardResponse dashboard = adminService.getDashboardData(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/system-info")
    @Operation(summary = "System info", description = "Get system information")
    public ResponseEntity<ApiResponse<SystemInfoResponse>> getSystemInfo() {
        SystemInfoResponse info = adminService.getSystemInfo();
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @PostMapping("/config")
    @Operation(summary = "Update config", description = "Update system configuration")
    public ResponseEntity<ApiResponse<Void>> updateSystemConfig(
            @Valid @RequestBody SystemConfigRequest request) {
        adminService.updateSystemConfig(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Configuration updated"));
    }

    @GetMapping("/export/{type}")
    @Operation(summary = "Export data", description = "Export data to Excel/CSV")
    public ResponseEntity<byte[]> exportData(
            @PathVariable String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] data = adminService.exportData(type, startDate, endDate);
        String filename = String.format("%s-export-%s.xlsx", type, LocalDate.now());

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(data);
    }
}