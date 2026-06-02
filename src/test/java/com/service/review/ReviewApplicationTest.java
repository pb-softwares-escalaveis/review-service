package com.service.review;

import com.service.review.domain.Auction;
import com.service.review.dto.ReviewResponse;
import com.service.review.service.kafka.KafkaReviewPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReviewApplicationTest {
    @Autowired
    KafkaReviewPublisher kafkaReviewPublisher;

    @Test
    void contextLoads() {
        ReviewResponse reviewResponse = new ReviewResponse(true, "bom");
        kafkaReviewPublisher.publishAuctionReview(reviewResponse);
    }
}