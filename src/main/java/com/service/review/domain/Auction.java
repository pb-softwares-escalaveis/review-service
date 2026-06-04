package com.service.review.domain;

import com.service.review.kafka.events.AuctionCreatedPendingReview;
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
    private Long auctionId;
    private UUID sellerId;
    private String sellerName;
    private String sellerEmail;
    private String auctionTitle;
    private String auctionDescription;
    private Instant ocurredAt;
    private String auctionThumb;
    private UUID correlationId;
    private ReviewContext reviewContext;

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
        this.reviewContext = ReviewContext.AUCTION;
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

    @Override
    public String toString() {
        return "Auction{" +
                "auctionTitle='" + auctionTitle + '\'' +
                ", auctionDescription='" + auctionDescription + '\'' +
                ", auctionThumb='" + auctionThumb + '\'' +
                '}';
    }
}
