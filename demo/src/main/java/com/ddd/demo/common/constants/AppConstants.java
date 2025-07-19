package com.ddd.demo.common.constants;

public final class AppConstants {

    public static final class Security {
        public static final String JWT_TOKEN_PREFIX = "Bearer ";
        public static final String JWT_HEADER_NAME = "Authorization";
        public static final String ROLE_USER = "ROLE_USER";
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
    }

    public static final class Cache {
        public static final String USERS_CACHE = "users";
        public static final String PRODUCTS_CACHE = "products";
        public static final String ORDERS_CACHE = "orders";
    }

    public static final class Validation {
        public static final int MIN_PASSWORD_LENGTH = 6;
        public static final int MAX_PASSWORD_LENGTH = 100;
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 50;
    }

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}s