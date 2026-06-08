package com.service.review.domain;

import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.kafka.events.MessageCreatedPendingReview;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Message {
    private Long auctionId;
    private UUID sellerId;
    private Long messageId;
    private String sellerName;
    private String sellerEmail;
    private String message;
    private Instant ocurredAt;
    private UUID correlationId;

    public Message(Long auctionId, UUID sellerId, Long messageId, String sellerName, String sellerEmail, String message, Instant ocurredAt, UUID correlationId) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.messageId = messageId;
        this.sellerName = sellerName;
        this.sellerEmail = sellerEmail;
        this.message = message;
        this.ocurredAt = ocurredAt;
        this.correlationId = correlationId;
    }

    public static Message from(MessageCreatedPendingReview event) {
        return new Message(
                event.auctionId(),
                event.sellerId(),
                event.messageId(),
                event.sellerName(),
                event.sellerEmail(),
                event.message(),
                event.ocurredAt(),
                event.correlationId()
        );
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                '}';
    }
}
