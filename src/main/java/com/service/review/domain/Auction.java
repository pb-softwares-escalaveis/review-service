package com.service.review.domain;

import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.kafka.events.AuctionReportedPendingReview;
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
public class Auction {
    private UUID userId;
    private Long auctionId;
    private UUID sellerId;
    private String sellerName;
    private String sellerEmail;
    private String auctionTitle;
    private String auctionDescription;
    private String reportReason;
    private Instant ocurredAt;
    private String auctionThumb;
    private UUID correlationId;

    public Auction(Long auctionId, UUID sellerId, String sellerName, String sellerEmail, String auctionTitle, String auctionDescription, Instant ocurredAt, String auctionThumb, UUID correlationId) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerEmail = sellerEmail;
        this.auctionTitle = auctionTitle;
        this.auctionDescription = auctionDescription;
        this.ocurredAt = ocurredAt;
        this.auctionThumb = auctionThumb;
        this.correlationId = correlationId;
    }

    public Auction(UUID userId, Long auctionId, UUID sellerId, String sellerName, String sellerEmail, String auctionTitle, String auctionDescription, String reportReason, Instant ocurredAt, String auctionThumb, UUID correlationId) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerEmail = sellerEmail;
        this.auctionTitle = auctionTitle;
        this.auctionDescription = auctionDescription;
        this.reportReason = reportReason;
        this.ocurredAt = ocurredAt;
        this.auctionThumb = auctionThumb;
        this.correlationId = correlationId;
    }

    public Auction(UUID userId, Long auctionId, UUID sellerId, String auctionTitle, String auctionDescription, String reportReason, Instant ocurredAt, String auctionThumb, UUID correlationId) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.auctionTitle = auctionTitle;
        this.auctionDescription = auctionDescription;
        this.reportReason = reportReason;
        this.ocurredAt = ocurredAt;
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
                event.ocurredAt(),
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
                event.reportReason(),
                event.ocurredAt(),
                event.auctionThumb(),
                event.correlationId()
        );
    }




    @Override
    public String toString() {
        String base = "Auction{" +
                "auctionTitle='" + auctionTitle + '\'' +
                ", auctionDescription='" + auctionDescription + '\'' +
                ", auctionThumb='" + auctionThumb + '\'';

        if (reportReason != null && !reportReason.isBlank()) {
            base += ", reportReason='" + reportReason + '\'';
        }

        return base + '}';
    }
}