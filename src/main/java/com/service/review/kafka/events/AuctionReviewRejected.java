package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReviewRejected(
        Long auctionId,
        UUID sellerId,
        String sellerName,
        String sellerEmail,
        String reason,
        String auctionTitle,
        String auctionThumb,
        Instant ocurredAt,
        UUID correlationId
) implements ReviewEvent {
}
