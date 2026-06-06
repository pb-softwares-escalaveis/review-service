package com.service.review.service;

import com.service.review.domain.Auction;
import com.service.review.domain.ReviewAuction;
import com.service.review.dto.ReviewResponse;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.AuctionReviewApproved;
import com.service.review.kafka.events.AuctionReviewRejected;
import com.service.review.kafka.events.ReviewEvent;
import com.service.review.repository.ReviewAuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewAuctionService {
    private final ReviewAuctionRepository reviewAuctionRepository;
    private final ReviewService reviewService;
    private final KafkaService kafkaService;

    public ReviewResponse reviewAuction(Auction auction) {
        ReviewResponse reviewResponse = analyzeAuction(auction);
        persistReview(auction, reviewResponse);
        publishEvent(auction, reviewResponse);
        return reviewResponse;
    }

    private ReviewResponse analyzeAuction(Auction auction) {
        return reviewService.analyze(
                auction.toString(),
                auction.getReviewContext()
        );
    }

    private void persistReview(Auction auction, ReviewResponse reviewResponse) {
        ReviewAuction reviewAuction = new ReviewAuction(
                auction.getAuctionId(),
                auction.getSellerId(),
                reviewResponse.approved(),
                reviewResponse.reason()
        );
        reviewAuctionRepository.save(reviewAuction);
    }

    private void publishEvent(Auction auction, ReviewResponse reviewResponse) {
        ReviewEvent reviewEvent;
        if (reviewResponse.approved()) {
            reviewEvent = new AuctionReviewApproved(
                    auction.getAuctionId(),
                    auction.getSellerId(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        } else {
            reviewEvent = new AuctionReviewRejected(
                    auction.getAuctionId(),
                    auction.getSellerId(),
                    reviewResponse.reason(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        }
        kafkaService.sendEvent(reviewEvent);
    }
}