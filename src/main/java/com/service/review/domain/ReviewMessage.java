package com.service.review.domain;

import com.service.review.enums.ContextType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_reviews")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "auction_id")
    private Long auctionId;

    @Column(nullable = false, name = "seller_id")
    private UUID sellerId;

    @Column(nullable = false, name = "id_message")
    private Long messageId;

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

    @ManyToOne
    @JoinColumn(name = "id_context")
    ReviewMessageContext reviewMessageContext;

    public ReviewMessage(Long auctionId, UUID sellerId, Long messageId, Boolean approved, String repprovedReason, ReviewMessageContext reviewMessageContext) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.messageId = messageId;
        this.repprovedReason = repprovedReason;
        this.approved = approved;
        this.creationDate = Instant.now();
        this.reviewMessageContext = reviewMessageContext;
    }

    public ReviewMessage(Long auctionId, UUID sellerId, Long messageId, Boolean approved, String repprovedReason, String reportReason, ContextType type, ReviewMessageContext reviewMessageContext) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.messageId = messageId;
        this.approved = approved;
        this.repprovedReason = repprovedReason;
        this.reportReason = reportReason;
        this.type = type;
        this.creationDate = Instant.now();
        this.reviewMessageContext = reviewMessageContext;
    }
}
