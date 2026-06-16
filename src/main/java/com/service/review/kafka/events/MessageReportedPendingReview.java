package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record MessageReportedPendingReview(
        UUID userId,
        Long auctionId,
        UUID sellerId,
        Long messageId,
        String message,
        String reportReason,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
