package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record MessageReportApproved(
        Long auctionId,
        UUID sellerId,
        UUID userId,
        Long messageId,
        String message,
        String reprovedReason,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
