package com.service.review.service.kafka;

import com.service.review.domain.Auction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReviewListener {
    @KafkaListener(topics = "new-auction", groupId = "review-auction-consumer")
    public void consumeAuction(String title, String description, String category, String image) {
        Auction auction = new Auction(title, description, category, image);
        log.info("Auction consumida do Kafka: {}", auction);
    }

    @KafkaListener(topics = "message-reviewed", groupId = "review-message-consumer")
    public void consumeMessage(String payload) {
        log.info("Message consumida do Kafka: {}", "teste");
    }
}
