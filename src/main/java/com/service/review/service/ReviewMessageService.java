package com.service.review.service;

import com.service.review.domain.Message;
import com.service.review.domain.ReviewMessage;
import com.service.review.dto.ReviewResponse;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.*;
import com.service.review.repository.ReviewMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewMessageService {
    private final ReviewMessageRepository reviewMessageRepository;
    private final ReviewService reviewService;
    private final KafkaService kafkaService;

    public ReviewResponse reviewMessage(Message message) {
        ReviewResponse reviewResponse = analyzeMessage(message);
        persistReview(message, reviewResponse);
        publishEvent(message, reviewResponse);
        return reviewResponse;
    }

    private ReviewResponse analyzeMessage(Message message) {
        return reviewService.analyze(
                message.toString(),
                message.getReviewContext()
        );
    }

    private void persistReview(Message message, ReviewResponse reviewResponse) {
        ReviewMessage reviewMessage = new ReviewMessage(
                message.getAuctionId(),
                message.getSellerId(),
                message.getMessageId(),
                reviewResponse.approved(),
                reviewResponse.reason()
        );
        reviewMessageRepository.save(reviewMessage);
    }

    private void publishEvent(Message message, ReviewResponse reviewResponse) {
        ReviewEvent reviewEvent;
        if (reviewResponse.approved()) {
            reviewEvent = new MessageReviewApproved(
                    message.getAuctionId(),
                    message.getSellerId(),
                    message.getMessageId(),
                    message.getSellerName(),
                    message.getSellerEmail(),
                    message.getMessage(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        } else {
            reviewEvent = new MessageReviewRejected(
                    message.getAuctionId(),
                    message.getSellerId(),
                    message.getMessageId(),
                    message.getSellerName(),
                    message.getSellerEmail(),
                    reviewResponse.reason(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        }
        kafkaService.sendEvent(reviewEvent);
    }
}
