package com.service.review.service;

import com.service.review.domain.Auction;
import com.service.review.dto.ReviewResponse;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.kafka.events.AuctionReviewApproved;
import com.service.review.kafka.events.AuctionReviewRejected;
import com.service.review.kafka.events.ReviewEvent;
import com.service.review.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ReviewService reviewService;
    private final KafkaService kafkaService;

    public Auction saveNewAuction(Auction auction) {
        return auctionRepository.save(auction);
    }

    public ReviewResponse reviewAuction(Auction auction) {
        ReviewResponse reviewResponse = reviewService.analyze(
                auction.toString(),
                auction.getReviewContext()
        );

        ReviewEvent reviewEvent;
        if (reviewResponse.approved()) {
            reviewEvent = new AuctionReviewApproved(
                    auction.getAuctionId(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        } else {
            reviewEvent = new AuctionReviewRejected(
                    auction.getAuctionId(),
                    reviewResponse.reason(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        }
        kafkaService.sendEvent(reviewEvent);
        return reviewResponse;
    }
}