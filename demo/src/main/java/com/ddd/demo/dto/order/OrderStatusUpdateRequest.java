package com.ddd.demo.dto.order;

import com.ddd.demo.entity.order.Order;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private Order.OrderStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // For shipping status
    private String trackingNumber;

    // For cancellation
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String cancellationReason;
}