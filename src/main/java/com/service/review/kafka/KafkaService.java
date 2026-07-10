package com.service.review.kafka;

import com.service.review.kafka.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

    @Value("${app.kafka-topics.reviews.report.auction-approved}")
    private String AUCTION_REPORT_APPROVED;

    @Value("${app.kafka-topics.reviews.report.qa-approved}")
    private String MESSAGE_REPORT_APPROVED;

    @Value("${app.kafka-topics.reviews.auction.approved}")
    private String AUCTION_REVIEW_APPROVED;

    @Value("${app.kafka-topics.reviews.auction.rejected}")
    private String AUCTION_REVIEW_REJECTED;

    @Value("${app.kafka-topics.reviews.qa.approved}")
    private String QA_REVIEW_APPROVED;

    @Value("${app.kafka-topics.reviews.qa.rejected}")
    private String QA_REVIEW_REJECTED;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    public void sendEvent(ReviewEvent event) {
        String kafkaKey = event.auctionId().toString();

        String topic = switch (event) {
            case AuctionReviewApproved ignored -> AUCTION_REVIEW_APPROVED;
            case AuctionReviewRejected ignored -> AUCTION_REVIEW_REJECTED;
            case MessageReviewApproved ignored -> QA_REVIEW_APPROVED;
            case MessageReviewRejected ignored -> QA_REVIEW_REJECTED;
            case AuctionReportApproved ignored -> AUCTION_REPORT_APPROVED;
            case MessageReportApproved ignored -> MESSAGE_REPORT_APPROVED;
            default -> throw new IllegalArgumentException(
                    "Evento não mapeado para envio: " + event.getClass().getSimpleName());
        };

        try {
            String jsonMessage = jsonMapper.writeValueAsString(event);
            log.debug("Publicando evento no Kafka. tipo={} | topic={} | key={} | message={}",
                    event.getClass().getSimpleName(), topic, kafkaKey, jsonMessage);

            kafkaTemplate.send(topic, kafkaKey, jsonMessage)
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
        } catch (Exception e) {
            log.error("Erro ao serializar evento para JSON. tipo={} | error={}",
                    event.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Erro ao serializar evento para JSON", e);
        }
    }
}