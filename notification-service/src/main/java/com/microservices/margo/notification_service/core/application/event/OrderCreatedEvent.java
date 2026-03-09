package com.microservices.margo.notification_service.core.application.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID correlationId,
        UUID coreItemId,
        UUID ownerUserId,
        String summary
) {}
