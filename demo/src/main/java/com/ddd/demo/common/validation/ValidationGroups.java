package com.ddd.demo.common.validation;

public interface ValidationGroups {

    // Basic CRUD operations
    interface Create {}
    interface Update {}
    interface Delete {}

    // User-specific validations
    interface UserRegistration extends Create {}
    interface UserUpdate extends Update {}
    interface AdminUserCreate extends Create {}

    // Product-specific validations
    interface ProductCreate extends Create {}
    interface ProductUpdate extends Update {}
    interface ProductPublish {}

    // Order-specific validations
    interface OrderCreate extends Create {}
    interface OrderUpdate extends Update {}
    interface OrderCancel {}
    interface OrderConfirm {}

    // Payment validations
    interface PaymentProcess {}
    interface PaymentRefund {}
}