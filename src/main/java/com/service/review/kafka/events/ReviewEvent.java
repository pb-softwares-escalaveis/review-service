package com.service.review.kafka.events;

import java.time.Instant;
import java.util.UUID;

public interface ReviewEvent {
    Long auctionId();
    UUID sellerId();
    Instant ocurredAt();
    UUID correlationId();
}
