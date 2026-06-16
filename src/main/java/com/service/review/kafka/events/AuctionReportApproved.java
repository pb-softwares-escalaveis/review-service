package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReportApproved(
        Long auctionId,
        UUID sellerId,
        String repprovedReason,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
