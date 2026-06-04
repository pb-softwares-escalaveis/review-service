package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionCreatedPendingReview(
        Long auctionId,
        UUID sellerId,
        String sellerName,
        String sellerEmail,
        String auctionTitle,
        String auctionDescription,
        Instant ocurredAt,
        String auctionThumb,
        UUID correlationId
) {
}
