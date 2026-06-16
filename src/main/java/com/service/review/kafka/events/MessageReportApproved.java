package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record MessageReportApproved(
        Long auctionId,
        UUID sellerId,
        Long messageId,
        String repprovedReason,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
