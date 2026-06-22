package com.service.review.service;

import com.service.review.domain.Message;
import com.service.review.domain.ReviewMessage;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.dto.ReviewResponse;
import com.service.review.enums.ContextType;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.*;
import com.service.review.metrics.ReviewMetrics;
import com.service.review.repository.ReviewMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ReviewMetrics reviewMetrics;

    public ReviewResponse reviewMessage(Message message, ReviewMessageContext reviewMessageContext, UUID userId) {
        log.info("Iniciando revisão da mensagem. auctionId={} | messageId={} | sellerId={}",
                message.getAuctionId(), message.getMessageId(), message.getSellerId());

        ContextType contextType = reviewMessageContext.getType();
        long start = System.currentTimeMillis();

        try {
            ReviewResponse reviewResponse = analyzeMessage(message, reviewMessageContext, contextType);
            persistReview(message, reviewMessageContext, reviewResponse, contextType);
            publishEvent(message, reviewResponse, reviewMessageContext, userId, contextType);

            reviewMetrics.recordProcessingTime("message", contextType, "success", System.currentTimeMillis() - start);
            log.info("Revisão da mensagem concluída. auctionId={} | messageId={} | approved={} | reason='{}'",
                    message.getAuctionId(), message.getMessageId(),
                    reviewResponse.approved(), reviewResponse.repprovedReason());

            return reviewResponse;
        } catch (Exception e) {
            reviewMetrics.recordProcessingTime("message", contextType, "failure", System.currentTimeMillis() - start);
            throw e;
        }
    }

    private ReviewResponse analyzeMessage(Message message, ReviewMessageContext reviewMessageContext, ContextType contextType) {
        log.debug("Enviando mensagem para análise de IA. messageId={} | auctionId={} | contextId={}",
                message.getMessageId(), message.getAuctionId(), reviewMessageContext.getId());

        ReviewResponse response = reviewService.analyze(message.toString(), reviewMessageContext.getContext());

        if (response.approved()) {
            reviewMetrics.incrementApproved("message", contextType);
        } else {
            reviewMetrics.incrementRejected("message", contextType);
        }

        log.debug("Análise de IA recebida para mensagem. messageId={} | approved={} | reason='{}'",
                message.getMessageId(), response.approved(), response.repprovedReason());

        return response;
    }

    private void persistReview(Message message, ReviewMessageContext reviewMessageContext,
                               ReviewResponse reviewResponse, ContextType contextType) {
        log.debug("Persistindo revisão da mensagem. messageId={} | auctionId={} | approved={}",
                message.getMessageId(), message.getAuctionId(), reviewResponse.approved());

        ReviewMessage reviewMessage = new ReviewMessage(
                message.getAuctionId(), message.getSellerId(), message.getMessageId(),
                reviewResponse.approved(), reviewResponse.repprovedReason(),
                message.getReportReason(), contextType, reviewMessageContext
        );

        try {
            ReviewMessage saved = reviewMessageRepository.save(reviewMessage);
            log.info("Revisão da mensagem persistida com sucesso. reviewId={} | messageId={} | auctionId={} | approved={}",
                    saved.getId(), message.getMessageId(), message.getAuctionId(), reviewResponse.approved());
        } catch (Exception e) {
            reviewMetrics.incrementPersistFailure("message", contextType);
            log.error("Falha ao persistir revisão da mensagem. messageId={} | auctionId={} | erro={}",
                    message.getMessageId(), message.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }

    private void publishEvent(Message message, ReviewResponse reviewResponse,
                              ReviewMessageContext reviewMessageContext, UUID userId, ContextType contextType) {
        boolean isReport = contextType == ContextType.REPORTED;
        ReviewEvent reviewEvent;
        String eventType;

        if (reviewResponse.approved()) {
            if (isReport) {
                eventType = "report_approved";
                log.info("Report de mensagem APROVADO — publicando evento MessageReportApproved. messageId={} | auctionId={} | sellerId={}",
                        message.getMessageId(), message.getAuctionId(), message.getSellerId());
                reviewEvent = new MessageReportApproved(message.getAuctionId(), message.getSellerId(), userId,
                        message.getMessageId(), message.getMessage(), reviewResponse.repprovedReason(),
                        Instant.now(), UUID.randomUUID());
            } else {
                eventType = "approved";
                log.info("Mensagem APROVADA — publicando evento MessageReviewApproved. messageId={} | auctionId={} | sellerId={}",
                        message.getMessageId(), message.getAuctionId(), message.getSellerId());
                reviewEvent = new MessageReviewApproved(message.getAuctionId(), message.getSellerId(),
                        message.getMessageId(), Instant.now(), UUID.randomUUID());
            }
        } else {
            eventType = "rejected";
            log.info("Mensagem REPROVADA — publicando evento MessageReviewRejected. messageId={} | auctionId={} | sellerId={} | reason='{}'",
                    message.getMessageId(), message.getAuctionId(), message.getSellerId(), reviewResponse.repprovedReason());
            reviewEvent = new MessageReviewRejected(message.getAuctionId(), message.getSellerId(),
                    message.getMessageId(), reviewResponse.repprovedReason(), Instant.now(), UUID.randomUUID());
        }

        try {
            kafkaService.sendEvent(reviewEvent);
            reviewMetrics.incrementEventPublished("message", contextType, eventType);
            log.debug("Evento de revisão publicado no Kafka. tipo={} | messageId={} | auctionId={}",
                    reviewEvent.getClass().getSimpleName(), message.getMessageId(), message.getAuctionId());
        } catch (Exception e) {
            reviewMetrics.incrementEventPublishFailure("message", contextType, eventType);
            log.error("Falha ao publicar evento no Kafka. tipo={} | messageId={} | auctionId={} | erro={}",
                    reviewEvent.getClass().getSimpleName(), message.getMessageId(), message.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }
}