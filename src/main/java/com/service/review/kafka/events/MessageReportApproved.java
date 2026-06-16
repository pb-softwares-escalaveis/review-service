package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record MessageReportApproved(
        UUID userId,
        UUID sellerId,
        Long auctionId,
        Long messageId,
        String message,
        String reportReason,
        String repprovedReason,
        Instant occurredAt,
        UUID correlationId
) implements ReviewEvent {
}
