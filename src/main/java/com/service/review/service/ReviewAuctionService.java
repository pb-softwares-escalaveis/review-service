package com.service.review.service;

import com.service.review.domain.Auction;
import com.service.review.domain.ReviewAuction;
import com.service.review.domain.ReviewAuctionContext;
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

    public ReviewResponse reviewAuction(Auction auction, ReviewAuctionContext reviewAuctionContext) {
        ReviewResponse reviewResponse = analyzeAuction(auction, reviewAuctionContext);
        persistReview(auction, reviewAuctionContext, reviewResponse);
        publishEvent(auction, reviewResponse);
        return reviewResponse;
    }

    private ReviewResponse analyzeAuction(Auction auction, ReviewAuctionContext reviewAuctionContext) {
        return reviewService.analyze(
                auction.toString(),
                reviewAuctionContext.getContext()
        );
    }

    private void persistReview(Auction auction, ReviewAuctionContext reviewAuctionContext, ReviewResponse reviewResponse) {
        ReviewAuction reviewAuction = new ReviewAuction(
                auction.getAuctionId(),
                auction.getSellerId(),
                reviewResponse.approved(),
                reviewResponse.reason(),
                reviewAuctionContext
        );
        reviewAuctionRepository.save(reviewAuction);
    }

    private void publishEvent(Auction auction, ReviewResponse reviewResponse) {
        ReviewEvent reviewEvent;
        if (reviewResponse.approved()) {
            reviewEvent = new AuctionReviewApproved(
                    auction.getAuctionId(),
                    auction.getSellerId(),
                    auction.getSellerName(),
                    auction.getSellerEmail(),
                    auction.getAuctionTitle(),
                    auction.getAuctionThumb(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        } else {
            reviewEvent = new AuctionReviewRejected(
                    auction.getAuctionId(),
                    auction.getSellerId(),
                    auction.getSellerName(),
                    auction.getSellerEmail(),
                    reviewResponse.reason(),
                    auction.getAuctionTitle(),
                    auction.getAuctionThumb(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        }
        kafkaService.sendEvent(reviewEvent);
    }
}