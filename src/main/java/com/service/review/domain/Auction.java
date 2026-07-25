package com.service.review.domain;

import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.kafka.events.AuctionReportedPendingReview;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Auction {
    private UUID userId;
    private Long auctionId;
    private UUID sellerId;
    private String sellerName;
    private String sellerEmail;
    private String auctionTitle;
    private String auctionDescription;
    private String auctionCategory;
    private String reportReason;
    private Instant occurredAt;
    private String auctionThumb;
    private UUID correlationId;

    public Auction(Long auctionId, UUID sellerId, String sellerName, String sellerEmail, String auctionTitle, String auctionDescription, String auctionCategory, Instant occurredAt, String auctionThumb, UUID correlationId) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerEmail = sellerEmail;
        this.auctionTitle = auctionTitle;
        this.auctionDescription = auctionDescription;
        this.auctionCategory = auctionCategory;
        this.occurredAt = occurredAt;
        this.auctionThumb = auctionThumb;
        this.correlationId = correlationId;
    }

    public Auction(UUID userId, Long auctionId, UUID sellerId, String auctionTitle, String auctionDescription, String auctionCategory, String reportReason, Instant occurredAt, String auctionThumb, UUID correlationId) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.auctionTitle = auctionTitle;
        this.auctionDescription = auctionDescription;
        this.auctionCategory = auctionCategory;
        this.reportReason = reportReason;
        this.occurredAt = occurredAt;
        this.auctionThumb = auctionThumb;
        this.correlationId = correlationId;
    }

    public static Auction from(AuctionCreatedPendingReview event) {
        return new Auction(
                event.auctionId(),
                event.sellerId(),
                event.sellerName(),
                event.sellerEmail(),
                event.auctionTitle(),
                event.auctionDescription(),
                event.auctionCategory(),
                event.occurredAt(),
                event.auctionThumb(),
                event.correlationId()
        );
    }

    public static Auction from(AuctionReportedPendingReview event) {
        return new Auction(
                event.userId(),
                event.auctionId(),
                event.sellerId(),
                event.auctionTitle(),
                event.auctionDescription(),
                event.auctionCategory(),
                event.reportReason(),
                event.occurredAt(),
                event.auctionThumb(),
                event.correlationId()
        );
    }
}