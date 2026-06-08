package com.service.review.service;

import com.service.review.domain.Message;
import com.service.review.domain.ReviewMessage;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.dto.ReviewResponse;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.*;
import com.service.review.repository.ReviewMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewMessageService {
    private final ReviewMessageRepository reviewMessageRepository;
    private final ReviewService reviewService;
    private final KafkaService kafkaService;

    public ReviewResponse reviewMessage(Message message, ReviewMessageContext reviewMessageContext) {
        log.info("Iniciando revisão da mensagem. auctionId={} | messageId={} | sellerId={}",
                message.getAuctionId(), message.getMessageId(), message.getSellerId());

        ReviewResponse reviewResponse = analyzeMessage(message, reviewMessageContext);
        persistReview(message, reviewMessageContext, reviewResponse);
        publishEvent(message, reviewResponse);

        log.info("Revisão da mensagem concluída. auctionId={} | messageId={} | approved={} | reason='{}'",
                message.getAuctionId(), message.getMessageId(),
                reviewResponse.approved(), reviewResponse.reason());

        return reviewResponse;
    }

    private ReviewResponse analyzeMessage(Message message, ReviewMessageContext reviewMessageContext) {
        log.debug("Enviando mensagem para análise de IA. messageId={} | auctionId={} | contextId={}",
                message.getMessageId(), message.getAuctionId(), reviewMessageContext.getId());

        ReviewResponse response = reviewService.analyze(
                message.toString(),
                reviewMessageContext.getContext()
        );

        log.debug("Análise de IA recebida para mensagem. messageId={} | approved={} | reason='{}'",
                message.getMessageId(), response.approved(), response.reason());

        return response;
    }

    private void persistReview(Message message, ReviewMessageContext reviewMessageContext, ReviewResponse reviewResponse) {
        log.debug("Persistindo revisão da mensagem. messageId={} | auctionId={} | approved={}",
                message.getMessageId(), message.getAuctionId(), reviewResponse.approved());

        ReviewMessage reviewMessage = new ReviewMessage(
                message.getAuctionId(),
                message.getSellerId(),
                message.getMessageId(),
                reviewResponse.approved(),
                reviewResponse.reason(),
                reviewMessageContext
        );

        ReviewMessage saved = reviewMessageRepository.save(reviewMessage);
        log.info("Revisão da mensagem persistida com sucesso. reviewId={} | messageId={} | auctionId={} | approved={}",
                saved.getId(), message.getMessageId(), message.getAuctionId(), reviewResponse.approved());
    }

    private void publishEvent(Message message, ReviewResponse reviewResponse) {
        ReviewEvent reviewEvent;

        if (reviewResponse.approved()) {
            log.info("Mensagem APROVADA — publicando evento MessageReviewApproved. messageId={} | auctionId={} | sellerId={}",
                    message.getMessageId(), message.getAuctionId(), message.getSellerId());
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
            log.info("Mensagem REPROVADA — publicando evento MessageReviewRejected. messageId={} | auctionId={} | sellerId={} | reason='{}'",
                    message.getMessageId(), message.getAuctionId(),
                    message.getSellerId(), reviewResponse.reason());
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
        log.debug("Evento de revisão publicado no Kafka. tipo={} | messageId={} | auctionId={}",
                reviewEvent.getClass().getSimpleName(), message.getMessageId(), message.getAuctionId());
    }
}