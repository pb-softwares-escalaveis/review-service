package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReportedPendingReview(
        UUID userId,
        Long auctionId,
        UUID sellerId,
        String auctionTitle,
        String auctionDescription,
        String reportReason,
        Instant occurredAt,
        String auctionThumb,
        UUID correlationId
) implements ReviewEvent {
}
