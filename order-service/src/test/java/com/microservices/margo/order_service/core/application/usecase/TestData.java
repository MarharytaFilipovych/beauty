package com.microservices.margo.order_service.core.application.usecase;

import com.microservices.margo.order_service.core.domain.Order;
import com.microservices.margo.order_service.core.domain.OrderStatus;
import com.microservices.margo.order_service.core.infrastructure.entity.OrderEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestData {

    static OrderEntity buildEntity(UUID id) {
        return buildEntity(id, OrderStatus.PENDING);
    }

    static Order buildOrder(UUID id) {
        return buildOrder(id, OrderStatus.PENDING);
    }

    static Order buildOrder(UUID id, OrderStatus status) {
        return Order.builder()
                .id(id)
                .customerId(UUID.randomUUID())
                .itemName("Laptop")
                .quantity(1)
                .price(new BigDecimal("1499.99"))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    static OrderEntity buildEntity(UUID id, OrderStatus status) {
        OrderEntity e = new OrderEntity();
        e.setId(id);
        e.setCustomerId(UUID.randomUUID());
        e.setItemName("Laptop");
        e.setQuantity(1);
        e.setPrice(new BigDecimal("1499.99"));
        e.setStatus(status);
        return e;
    }
}
