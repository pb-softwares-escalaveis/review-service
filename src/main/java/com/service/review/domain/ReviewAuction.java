package com.service.review.domain;

import com.service.review.enums.ContextType;
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
    private String repprovedReason;

    @Column(name = "reportReason")
    private String reportReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "type")
    private ContextType type;

    @Column(nullable = false)
    private Instant creationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_context")
    private ReviewAuctionContext reviewAuctionContext;

    public ReviewAuction(Long auctionId, UUID sellerId, Boolean approved, String repprovedReason, ContextType type, ReviewAuctionContext reviewAuctionContext) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.approved = approved;
        this.repprovedReason = repprovedReason;
        this.type = type;
        this.reviewAuctionContext = reviewAuctionContext;
        this.creationDate = Instant.now();
    }

    public ReviewAuction(Long auctionId, UUID sellerId, Boolean approved, String repprovedReason, String reportReason, ContextType type, ReviewAuctionContext reviewAuctionContext) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.approved = approved;
        this.repprovedReason = repprovedReason;
        this.reportReason = reportReason;
        this.type = type;
        this.creationDate = Instant.now();
    }
}
