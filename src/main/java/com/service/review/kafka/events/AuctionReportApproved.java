package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReportApproved(
        Long auctionId,
        UUID sellerId,
        Instant ocurredAt,
        UUID correlationId
) implements ReviewEvent {
}
