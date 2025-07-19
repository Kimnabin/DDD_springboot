package com.ddd.demo.common.exception;

import lombok.Getter;

/**
 * Enumeration of application error codes for better error tracking and i18n support
 */
@Getter
public enum ErrorCode {

    // Authentication & Authorization errors (1000-1999)
    INVALID_CREDENTIALS("AUTH_001", "Invalid username or password"),
    ACCOUNT_DISABLED("AUTH_002", "User account is disabled"),
    ACCOUNT_LOCKED("AUTH_003", "User account is locked"),
    TOKEN_EXPIRED("AUTH_004", "Authentication token has expired"),
    TOKEN_INVALID("AUTH_005", "Invalid authentication token"),
    INSUFFICIENT_PERMISSIONS("AUTH_006", "Insufficient permissions to perform this action"),
    PASSWORD_EXPIRED("AUTH_007", "Password has expired"),

    // Validation errors (2000-2999)
    VALIDATION_FAILED("VAL_001", "Input validation failed"),
    REQUIRED_FIELD_MISSING("VAL_002", "Required field is missing"),
    INVALID_FORMAT("VAL_003", "Invalid data format"),
    DUPLICATE_VALUE("VAL_004", "Duplicate value not allowed"),
    VALUE_OUT_OF_RANGE("VAL_005", "Value is out of allowed range"),
    INVALID_EMAIL_FORMAT("VAL_006", "Invalid email format"),
    INVALID_PHONE_FORMAT("VAL_007", "Invalid phone number format"),
    PASSWORD_TOO_WEAK("VAL_008", "Password does not meet security requirements"),

    // Business logic errors (3000-3999)
    RESOURCE_NOT_FOUND("BUS_001", "Requested resource not found"),
    RESOURCE_ALREADY_EXISTS("BUS_002", "Resource already exists"),
    OPERATION_NOT_ALLOWED("BUS_003", "Operation not allowed in current state"),
    INSUFFICIENT_STOCK("BUS_004", "Insufficient stock available"),
    ORDER_CANNOT_BE_CANCELLED("BUS_005", "Order cannot be cancelled in current status"),
    PAYMENT_FAILED("BUS_006", "Payment processing failed"),
    INVALID_COUPON("BUS_007", "Invalid or expired coupon code"),

    // System errors (4000-4999)
    DATABASE_ERROR("SYS_001", "Database operation failed"),
    EXTERNAL_SERVICE_ERROR("SYS_002", "External service unavailable"),
    FILE_UPLOAD_ERROR("SYS_003", "File upload failed"),
    EMAIL_SEND_ERROR("SYS_004", "Failed to send email"),
    CACHE_ERROR("SYS_005", "Cache operation failed"),
    CONFIGURATION_ERROR("SYS_006", "System configuration error"),

    // Generic errors (9000-9999)
    INTERNAL_SERVER_ERROR("GEN_001", "Internal server error occurred"),
    SERVICE_UNAVAILABLE("GEN_002", "Service temporarily unavailable"),
    RATE_LIMIT_EXCEEDED("GEN_003", "Rate limit exceeded"),
    MAINTENANCE_MODE("GEN_004", "System is under maintenance"),
    UNKNOWN_ERROR("GEN_999", "An unknown error occurred");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", code, message);
    }
}