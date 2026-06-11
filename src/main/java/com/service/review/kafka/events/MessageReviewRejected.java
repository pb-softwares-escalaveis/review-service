package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record MessageReviewRejected(
        Long auctionId,
        UUID sellerId,
        Long messageId,
        String reason,
        Instant ocurredAt,
        UUID correlationId
) implements ReviewEvent {
}
