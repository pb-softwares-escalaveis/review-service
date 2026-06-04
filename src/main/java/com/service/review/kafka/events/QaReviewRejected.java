package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record QaReviewRejected(
        Long auctionId,
        String reason,
        Instant ocurredAt,
        UUID correlationId
) implements ReviewEvent {
}
