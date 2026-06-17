package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record MessageCreatedPendingReview(
        Long auctionId,
        UUID sellerId,
        UUID userId,
        Long messageId,
        String sellerName,
        String sellerEmail,
        String message,
        Instant occurredAt,
        UUID correlationId
) {
}
