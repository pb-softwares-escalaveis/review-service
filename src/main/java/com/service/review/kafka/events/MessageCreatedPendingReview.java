package com.service.review.kafka.events;

import com.service.review.domain.ReviewContext;

import java.time.Instant;
import java.util.UUID;

public record MessageCreatedPendingReview(
        Long auctionId,
        UUID sellerId,
        Long messageId,
        String sellerName,
        String sellerEmail,
        String message,
        Instant ocurredAt,
        UUID correlationId
) {
}
