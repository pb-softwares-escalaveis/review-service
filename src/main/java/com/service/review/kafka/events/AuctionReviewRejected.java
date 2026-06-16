package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReviewRejected(
        Long auctionId,
        UUID sellerId,
        String reason,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
