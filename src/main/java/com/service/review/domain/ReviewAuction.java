package com.service.review.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auction_reviews")
@NoArgsConstructor
@Getter
@Setter
public class ReviewAuction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "auction_id")
    private Long auctionId;

    @Column(nullable = false, name = "seller_id")
    private UUID sellerId;

    @Column(nullable = false, name = "approved")
    private Boolean approved;

    @Column(name = "reason")
    private String reason;

    @Column(nullable = false)
    private Instant creationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_context")
    private ReviewAuctionContext reviewAuctionContext;

    public ReviewAuction(Long auctionId, UUID sellerId, Boolean approved, String reason, ReviewAuctionContext reviewAuctionContext) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.approved = approved;
        this.reason = reason;
        this.reviewAuctionContext = reviewAuctionContext;
        this.creationDate = Instant.now();

    }
}
