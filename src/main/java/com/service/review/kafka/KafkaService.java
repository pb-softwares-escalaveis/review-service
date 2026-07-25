package com.service.review.kafka;

import com.service.review.kafka.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {
    @Value("${app.kafka-topics.reviews.report.auction-approved}")
    String AUCTION_REPORT_APPROVED;
    @Value("${app.kafka-topics.reviews.report.auction-rejected}")
    String AUCTION_REPORT_REJECTED;
    @Value("${app.kafka-topics.reviews.report.qa-approved}")
    String MESSAGE_REPORT_APPROVED;
    @Value("${app.kafka-topics.reviews.report.qa-rejected}")
    String MESSAGE_REPORT_REJECTED;
    @Value("${app.kafka-topics.reviews.auction.approved}")
    String AUCTION_REVIEW_APPROVED;
    @Value("${app.kafka-topics.reviews.auction.rejected}")
    String AUCTION_REVIEW_REJECTED;
    @Value("${app.kafka-topics.reviews.qa.approved}")
    String QA_REVIEW_APPROVED;
    @Value("${app.kafka-topics.reviews.qa.rejected}")
    String QA_REVIEW_REJECTED;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(ReviewEvent event) {
        String kafkaKey = event.auctionId().toString();

        String topic = switch (event) {
            case AuctionReviewApproved ignored -> AUCTION_REVIEW_APPROVED;
            case AuctionReviewRejected ignored -> AUCTION_REVIEW_REJECTED;
            case MessageReviewApproved ignored -> QA_REVIEW_APPROVED;
            case MessageReviewRejected ignored -> QA_REVIEW_REJECTED;
            case AuctionReportApproved ignored -> AUCTION_REPORT_APPROVED;
            case AuctionReportRejected ignored -> AUCTION_REPORT_REJECTED;
            case MessageReportApproved ignored -> MESSAGE_REPORT_APPROVED;
            case MessageReportRejected ignored -> MESSAGE_REPORT_REJECTED;
            default -> throw new IllegalArgumentException(
                    "Evento não mapeado para envio: " + event.getClass().getSimpleName());
        };

        log.debug("Publicando evento no Kafka. tipo={} | topic={} | key={}",
                event.getClass().getSimpleName(), topic, kafkaKey);

        kafkaTemplate.send(topic, kafkaKey, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Falha ao publicar evento no Kafka. tipo={} | topic={} | key={} | erro={}",
                                event.getClass().getSimpleName(), topic, kafkaKey, ex.getMessage(), ex);
                    } else {
                        log.info("Evento publicado com sucesso no Kafka. tipo={} | topic={} | key={} | partition={} | offset={}",
                                event.getClass().getSimpleName(),
                                topic,
                                kafkaKey,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
