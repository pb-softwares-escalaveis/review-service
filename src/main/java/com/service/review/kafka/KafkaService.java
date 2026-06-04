package com.service.review.kafka;


import com.service.review.kafka.events.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {
    @Value("${app.kafka-topics.review.auction-review-approved}")
    String AUCTION_REVIEW_APPROVED;
    @Value("${app.kafka-topics.review.auction-review-rejected}")
    String AUCTION_REVIEW_REJECTED;
    @Value("${app.kafka-topics.review.qa-review-approved}")
    String QA_REVIEW_APPROVED;
    @Value("${app.kafka-topics.review.qa-review-rejected}")
    String QA_REVIEW_REJECTED;

    private final KafkaTemplate<String, ReviewEvent> kafkaTemplate;

    public void sendEvent(ReviewEvent event) {
        String kafkaKey = event.auctionId().toString();

        String topic = switch (event) {
            case AuctionReviewApproved ignored -> AUCTION_REVIEW_APPROVED;
            case AuctionReviewRejected ignored -> AUCTION_REVIEW_REJECTED;
            case QaReviewApproved ignored -> QA_REVIEW_APPROVED;
            case QaReviewRejected ignored -> QA_REVIEW_REJECTED;

            default -> throw new IllegalArgumentException("Evento não mapeado para envio: " + event.getClass().getSimpleName());
        };
        kafkaTemplate.send(topic, kafkaKey, event);
    }
}
