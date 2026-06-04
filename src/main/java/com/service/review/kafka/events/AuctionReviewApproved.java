package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AuctionReviewApproved (
        Long auctionId,
        UUID sellerId,
        String sellerName,
        String sellerEmail,
        String auctionTitle,
        String auctionThumb,
        Instant ocurredAt,
        UUID correlationId
) implements ReviewEvent{
}
