package com.microservices.margo.order_service.core.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID correlationId,
        UUID orderId,
        UUID ownerUserId,
        String summary
) {}
