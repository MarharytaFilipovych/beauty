package com.microservices.margo.notification_service.core.infrastructure.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.margo.notification_service.core.infrastructure.config.RabbitMQConfig;
import com.microservices.margo.notification_service.core.infrastructure.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final StoreNotificationUseCase storeNotification;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent eventId={}", event.eventId());
        try {
            String payload = objectMapper.writeValueAsString(event);
            storeNotification.execute(event, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload", e);
        }
    }
}