package com.ddd.demo.common.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class CommonUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    public static String generateUniqueId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static String formatTimestamp(LocalDateTime dateTime) {
        return dateTime.format(TIMESTAMP_FORMATTER);
    }

    public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return PageRequest.of(page, size, sort);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private CommonUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}