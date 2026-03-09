package com.microservices.margo.notification_service.core.application.mapper;

import com.microservices.margo.notification_service.core.infrastructure.entity.NotificationEntity;
import com.microservices.margo.notification_service.core.application.event.OrderCreatedEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationEntity toEntity(OrderCreatedEvent event);
}
