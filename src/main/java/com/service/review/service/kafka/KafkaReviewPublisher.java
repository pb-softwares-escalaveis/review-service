package com.service.review.service.kafka;

import com.service.review.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaReviewPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publishAuctionReview(ReviewResponse reviewResponse) {
        kafkaTemplate.send("auction-reviewed", String.valueOf(reviewResponse.approved()), reviewResponse.reason());
    }

    public void publishMessageReview(ReviewResponse reviewResponse) {
        kafkaTemplate.send("message-reviewed", String.valueOf(reviewResponse.approved()), reviewResponse.reason());
    }
}
