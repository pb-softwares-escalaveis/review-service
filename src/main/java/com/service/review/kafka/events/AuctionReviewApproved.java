package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReviewApproved(
        Long auctionId,
        UUID sellerId,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
