package com.microservices.margo.cafetiria.core.application.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
        @NotBlank String itemName,
        @Min(value = 1) int quantity,
        @NotNull @Positive BigDecimal price,
        @NotNull UUID customerId
) {}