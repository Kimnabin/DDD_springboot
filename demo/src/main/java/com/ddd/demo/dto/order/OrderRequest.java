package com.ddd.demo.dto.order;

import com.ddd.demo.common.validation.ValidEnum;
import com.ddd.demo.entity.order.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Size(max = 50, message = "Order cannot contain more than 50 items")
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @Pattern(regexp = "^[A-Z0-9]{4,20}$", message = "Coupon code must be 4-20 alphanumeric characters")
    private String couponCode;

    @ValidEnum(enumClass = Order.PaymentMethod.class, message = "Invalid payment method")
    private String paymentMethod = "CASH_ON_DELIVERY";

    @ValidEnum(enumClass = Order.ShippingMethod.class, message = "Invalid shipping method")
    private String shippingMethod = "STANDARD";

    @DecimalMin(value = "0.01", message = "Subtotal must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Subtotal is too large")
    private BigDecimal subtotal;
}