package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record QaReviewApproved(
        Long auctionId,
        Instant ocurredAt,
        UUID correlationId
) implements ReviewEvent {
}
