package com.service.review.service;

import com.service.review.domain.Auction;
import com.service.review.domain.Message;
import com.service.review.dto.ReviewResponse;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.*;
import com.service.review.repository.AuctionRepository;
import com.service.review.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ReviewService reviewService;
    private final KafkaService kafkaService;

    public ReviewResponse reviewMessage(Message message) {
        ReviewResponse reviewResponse = reviewService.analyze(
                message.toString(),
                message.getReviewContext()
        );

        ReviewEvent event;
        if (reviewResponse.approved()) {
            event = new QaReviewApproved(
                    message.getId(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        } else {
            event = new QaReviewRejected(
                    message.getId(),
                    reviewResponse.reason(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        }
        kafkaService.sendEvent(event);
        return reviewResponse;
    }
}
