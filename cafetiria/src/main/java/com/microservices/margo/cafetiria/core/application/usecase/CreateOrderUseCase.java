package com.microservices.margo.cafetiria.core.application.usecase;

import com.microservices.margo.cafetiria.core.application.request.CreateOrderRequest;
import com.microservices.margo.cafetiria.core.application.mapper.OrderMapper;
import com.microservices.margo.cafetiria.core.domain.Order;
import com.microservices.margo.cafetiria.core.infrastructure.client.UserValidationClient;
import com.microservices.margo.cafetiria.core.infrastructure.entity.OrderEntity;
import com.microservices.margo.cafetiria.core.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final UserValidationClient userValidationClient;

    public Order execute(CreateOrderRequest command) {
        log.info("Validating user {} before placing order", command.customerId());
        userValidationClient.validateUserExists(command.customerId());

        log.info("Placing order for customer='{}', item='{}'",
                command.customerId(), command.itemName());
        OrderEntity entity = orderRepository.save(orderMapper.toEntity(command));
        return orderMapper.toDomain(entity);
    }
}
