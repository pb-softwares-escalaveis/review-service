package com.service.review.kafka.consumer;

import com.service.review.domain.Auction;
import com.service.review.domain.Message;
import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.kafka.events.MessageCreatedPendingReview;
import com.service.review.service.ReviewAuctionService;
import com.service.review.service.ReviewMessageService;
import com.service.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReviewConsumer {
    private final ReviewAuctionService reviewAuctionService;
    private final ReviewMessageService reviewMessageService;

    @KafkaListener(topics = "auction.lot.created-pending")
    public void consumeAuctionPendingReview(AuctionCreatedPendingReview event) {
        Auction auction = Auction.from(event);
        log.info("Auction consumida do Kafka: {}", auction);
        reviewAuctionService.reviewAuction(auction);
    }

    @KafkaListener(topics = "qa.created-pending")
    public void consumeMessagePendingReview(MessageCreatedPendingReview event) {
        Message message = Message.from(event);
        log.info("Message consumida do Kafka: {}", message);
        reviewMessageService.reviewMessage(message);
    }
}
