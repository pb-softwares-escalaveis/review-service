package com.service.review.kafka.consumer;

import com.service.review.domain.Auction;
import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.service.AuctionService;
import com.service.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReviewConsumer {
    private final AuctionService auctionService;
    private final ReviewService reviewService;

    @KafkaListener(topics = "auction.lot.created-pending")
    public void consumeAuctionPendingReview(AuctionCreatedPendingReview event) {
        Auction auction = new Auction(
                event.auctionId(),
                event.auctionTitle(),
                event.auctionDescription(),
                event.auctionThumb()
        );
        Auction auctionSaved = auctionService.saveNewAuction(auction);
        auctionService.reviewAuction(auctionSaved);

        log.info("Auction consumida do Kafka: {}", auctionSaved);
    }

    @KafkaListener(topics = "qa.created-pending")
    public void consumeMessagePendingReview(String payload) {
        log.info("Message consumida do Kafka: {}", "teste");
    }
}
