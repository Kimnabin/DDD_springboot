package com.ddd.demo.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigRequest {

    // General settings
    private GeneralSettings general;

    // Email settings
    private EmailSettings email;

    // Payment settings
    private PaymentSettings payment;

    // Security settings
    private SecuritySettings security;

    // Feature flags
    private Map<String, Boolean> features;

    @Data
    @Builder
    public static class GeneralSettings {
        private String siteName;
        private String siteUrl;
        private String contactEmail;
        private String timezone;
        private String currency;
        private Boolean maintenanceMode;
        private String maintenanceMessage;
    }

    @Data
    @Builder
    public static class EmailSettings {
        private String smtpHost;
        private Integer smtpPort;
        private String smtpUsername;
        private String smtpPassword;
        private Boolean smtpUseTls;
        private String fromEmail;
        private String fromName;
    }

    @Data
    @Builder
    public static class PaymentSettings {
        @Min(0)
        private BigDecimal minOrderAmount;

        @Min(0)
        private BigDecimal maxOrderAmount;

        @Min(0)
        private BigDecimal taxRate;

        @Min(0)
        private BigDecimal standardShippingFee;

        @Min(0)
        private BigDecimal expressShippingFee;

        @Min(0)
        private BigDecimal freeShippingThreshold;

        private Map<String, Boolean> enabledPaymentMethods;
    }

    @Data
    @Builder
    public static class SecuritySettings {
        @NotNull
        @Min(6)
        private Integer minPasswordLength;

        @NotNull
        private Boolean requireUppercase;

        @NotNull
        private Boolean requireLowercase;

        @NotNull
        private Boolean requireNumbers;

        @NotNull
        private Boolean requireSpecialChars;

        @NotNull
        @Min(1)
        private Integer maxLoginAttempts;

        @NotNull
        @Min(1)
        private Integer sessionTimeoutMinutes;

        @NotNull
        private Boolean enableTwoFactor;

        @NotNull
        private Boolean enableCaptcha;
    }
}