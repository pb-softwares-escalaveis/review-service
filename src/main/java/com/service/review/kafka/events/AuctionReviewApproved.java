package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReviewApproved(
        UUID userId,
        Long auctionId,
        UUID sellerId,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
