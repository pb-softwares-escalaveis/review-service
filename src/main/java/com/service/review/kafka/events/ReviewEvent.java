package com.service.review.kafka.events;

import java.time.Instant;

public interface ReviewEvent {
    Long auctionId();
    Instant ocurredAt();
}
