package com.service.review;

import com.service.review.dto.ReviewResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReviewApplicationTest {
    @Autowired
    KafkaReviewEvent kafkaReviewEvent;

    @Test
    void contextLoads() {
        ReviewResponse reviewResponse = new ReviewResponse(true, "bom");
        kafkaReviewEvent.publishAuctionReview(reviewResponse);
    }
}