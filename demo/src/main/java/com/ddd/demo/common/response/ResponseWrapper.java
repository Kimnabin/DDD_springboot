package com.ddd.demo.common.response;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;

/**
 * Global response wrapper to standardize API responses
 * Automatically wraps controller responses in ApiResponse format
 */
@Slf4j
@ControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Don't wrap responses that are already ApiResponse, error pages, or actuator endpoints
        String className = returnType.getDeclaringClass().getName();
        return !className.contains("BasicErrorController")
                && !className.contains("ActuatorEndpoint")
                && !className.contains("SwaggerUiController")
                && !returnType.getParameterType().equals(ApiResponse.class)
                && !returnType.getParameterType().equals(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NotNull MethodParameter returnType,
                                  @NotNull MediaType selectedContentType,
                                  @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NotNull ServerHttpRequest request,
                                  @NotNull ServerHttpResponse response) {

        // Skip wrapping for non-JSON responses
        if (!selectedContentType.includes(MediaType.APPLICATION_JSON)) {
            return body;
        }

        // Wrap successful responses
        if (body instanceof ApiResponse) {
            return body;
        }

        return ApiResponse.builder()
                .success(true)
                .data(body)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
