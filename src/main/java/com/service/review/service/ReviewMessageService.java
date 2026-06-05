package com.service.review.service;

import com.service.review.domain.Message;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.domain.ReviewMessage;
import com.service.review.domain.ReviewMessageContext;
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

    public ReviewResponse reviewMessage(Message message, ReviewMessageContext reviewMessageContext) {
        ReviewResponse reviewResponse = analyzeMessage(message, reviewMessageContext);
        persistReview(message, reviewMessageContext, reviewResponse);
        publishEvent(message, reviewResponse);
        return reviewResponse;
    }

    private ReviewResponse analyzeMessage(Message message, ReviewMessageContext reviewMessageContext) {
        return reviewService.analyze(
                message.toString(),
                reviewMessageContext.getContext()
        );
    }

    private void persistReview(Message message, ReviewMessageContext reviewMessageContext, ReviewResponse reviewResponse) {
        ReviewMessage reviewMessage = new ReviewMessage(
                message.getAuctionId(),
                message.getSellerId(),
                message.getMessageId(),
                reviewResponse.approved(),
                reviewResponse.reason(),
                reviewMessageContext
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
