package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReportApproved(
        Long auctionId,
        UUID sellerId,
        UUID userId,
        String removalReason,
        String auctionTitle,
        String auctionThumb,
        String auctionDescription,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}