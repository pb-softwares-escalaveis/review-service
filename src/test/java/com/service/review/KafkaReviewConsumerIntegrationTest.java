package com.service.review;

import com.service.review.kafka.consumer.KafkaReviewConsumer;
import com.service.review.kafka.events.AuctionCreatedPendingReview;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
class KafkaReviewConsumerIntegrationTest {
    @Autowired
    KafkaReviewConsumer kafkaReviewConsumer;

    @Test
    void contextLoads() {
        AuctionCreatedPendingReview auctionCreatedPendingReview = new AuctionCreatedPendingReview(
                1L,
                UUID.randomUUID(),
                "Gabriel",
                "gabriel@gmail.com",
                "Cadeira de 5 pes",
                "Cadeira totalmente funcional",
                Instant.now(),
                "bucket.oleiloeiroonline.top/auction-images/air_fryer_philips_walita.jpg",
                UUID.randomUUID()
        );

        kafkaReviewConsumer.consumeAuctionPendingReview(auctionCreatedPendingReview);
    }
}
