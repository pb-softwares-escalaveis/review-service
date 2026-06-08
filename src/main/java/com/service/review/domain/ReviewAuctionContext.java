package com.service.review.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_auction_context")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewAuctionContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "context", columnDefinition = "TEXT")
    private String context;
}
