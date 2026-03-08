package com.microservices.margo.order_service.core.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record Order(
        UUID id,

        @NotBlank(message = "Item name must be specified.")
        String itemName,

        @Min(value = 1, message = "Quantity must be at least 1.")
        int quantity,

        @NotNull(message = "Price name must be specified.")
        @PositiveOrZero(message = "Price cannot be negative.")
        BigDecimal price,

        @NotNull(message = "Customer id is required.")
        UUID customerId,

        OrderStatus status,
        LocalDateTime createdAt
) {
    public Order {
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }
    public Order changeStatus(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot change order status from %s to %s", status, newStatus));
        }
        return this.toBuilder().status(newStatus).build();
    }
}