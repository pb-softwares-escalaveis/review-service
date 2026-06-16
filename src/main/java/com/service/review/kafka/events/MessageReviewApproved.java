package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record MessageReviewApproved(
        Long auctionId,
        UUID sellerId,
        Long messageId,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
