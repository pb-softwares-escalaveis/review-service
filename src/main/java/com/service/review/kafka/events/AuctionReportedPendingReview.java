package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReportedPendingReview(
        UUID userId,
        Long auctionId,
        UUID sellerId,
        String sellerName,
        String sellerEmail,
        String auctionTitle,
        String auctionDescription,
        String reportReason,
        Instant ocurredAt,
        String auctionThumb,
        UUID correlationId
) implements ReviewEvent {
}
