package com.service.review.kafka.consumer;

import com.service.review.domain.Auction;
import com.service.review.domain.Message;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.kafka.events.MessageCreatedPendingReview;
import com.service.review.service.ReviewAuctionContextService;
import com.service.review.service.ReviewAuctionService;
import com.service.review.service.ReviewMessageContextService;
import com.service.review.service.ReviewMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReviewConsumer {
    private final ReviewAuctionService reviewAuctionService;
    private final ReviewAuctionContextService reviewAuctionContextService;
    private final ReviewMessageService reviewMessageService;
    private final ReviewMessageContextService reviewMessageContextService;

    @KafkaListener(topics = "auctions.lot.created-pending")
    public void consumeAuctionPendingReview(AuctionCreatedPendingReview event) {
        Auction auction = Auction.from(event);
        ReviewAuctionContext reviewAuctionContext = reviewAuctionContextService.getLastVersionOfAuctionReviewContext();
        log.info("Auction consumida do Kafka: {}", auction);
        reviewAuctionService.reviewAuction(auction, reviewAuctionContext);
    }

    @KafkaListener(topics = "qa.created-pending")
    public void consumeMessagePendingReview(MessageCreatedPendingReview event) {
        Message message = Message.from(event);
        ReviewMessageContext reviewMessageContext = reviewMessageContextService.getLastVersionOfMessageReviewContext();
        log.info("Message consumida do Kafka: {}", message);
        reviewMessageService.reviewMessage(message, reviewMessageContext);
    }
}
