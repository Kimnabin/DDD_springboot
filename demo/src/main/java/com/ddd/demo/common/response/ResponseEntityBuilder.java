// Additional utility class for response handling
package com.ddd.demo.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for creating standardized ResponseEntity objects
 */
public final class ResponseEntityBuilder {

    private ResponseEntityBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Resource created successfully"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null, "Operation completed successfully"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String error) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Bad Request", error));
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Not Found", error));
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String error) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Forbidden", error));
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String error) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized", error));
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal Server Error", error));
    }
}