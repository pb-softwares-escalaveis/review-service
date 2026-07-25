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
                    reviewResponse.approved(), reviewResponse.reprovedReason());

            return reviewResponse;
        } catch (Exception e) {
            reviewMetrics.recordProcessingTime("message", contextType, "failure", System.currentTimeMillis() - start);
            throw e;
        }
    }

    private ReviewResponse analyzeMessage(Message message, ReviewMessageContext reviewMessageContext, ContextType contextType) {
        log.debug("Enviando mensagem para análise de IA. messageId={} | auctionId={} | contextId={}",
                message.getMessageId(), message.getAuctionId(), reviewMessageContext.getId());

        String input = buildInput(message, contextType);
        ReviewResponse response = reviewService.analyze(input, reviewMessageContext.getContext());

        if (response.approved()) {
            reviewMetrics.incrementApproved("message", contextType);
        } else {
            reviewMetrics.incrementRejected("message", contextType);
        }

        log.debug("Análise de IA recebida para mensagem. messageId={} | approved={} | reason='{}'",
                message.getMessageId(), response.approved(), response.reprovedReason());

        return response;
    }

    private void persistReview(Message message, ReviewMessageContext reviewMessageContext,
                               ReviewResponse reviewResponse, ContextType contextType) {
        log.debug("Persistindo revisão da mensagem. messageId={} | auctionId={} | approved={}",
                message.getMessageId(), message.getAuctionId(), reviewResponse.approved());

        ReviewMessage reviewMessage = new ReviewMessage(
                message.getAuctionId(), message.getSellerId(), message.getMessageId(),
                reviewResponse.approved(), reviewResponse.reprovedReason(),
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

    private String buildInput(Message message, ContextType type) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mensagem: ").append(message.getMessage());

        if (type == ContextType.REPORTED && message.getReportReason() != null && !message.getReportReason().isBlank()) {
            sb.append("\n\nDenúncia: ").append(message.getReportReason());
        }

        return sb.toString();
    }

    private void publishEvent(Message message, ReviewResponse reviewResponse,
                              ReviewMessageContext reviewMessageContext, UUID userId, ContextType contextType) {
        boolean isReport = contextType == ContextType.REPORTED;
        ReviewEvent reviewEvent;
        String eventType;

        if (reviewResponse.approved()) {
            //CASO: Conteúdo APROVADO
            if (isReport) {
                //Conteúdo limpo + foi denúncia = Denúncia REJEITADA
                eventType = "report_rejected";
                log.info("Denúncia de mensagem REJEITADA (conteúdo aprovado). messageId={} | auctionId={} | reporterId={}",
                        message.getMessageId(), message.getAuctionId(), userId);
                reviewEvent = new MessageReportRejected(
                        message.getAuctionId(),
                        message.getSellerId(),
                        userId,
                        message.getMessageId(),
                        message.getMessage(),
                        "Conteúdo da mensagem está dentro das regras",
                        Instant.now(),
                        UUID.randomUUID()
                );
            } else {
                // Criação normal aprovada
                eventType = "approved";
                log.info("Mensagem APROVADA. messageId={} | auctionId={} | sellerId={}",
                        message.getMessageId(), message.getAuctionId(), message.getSellerId());
                reviewEvent = new MessageReviewApproved(
                        message.getAuctionId(),
                        message.getSellerId(),
                        message.getMessageId(),
                        Instant.now(),
                        UUID.randomUUID()
                );
            }
        } else {
            // CASO: Conteúdo REPROVADO (viola regras)
            String reason = reviewResponse.reprovedReason();

            if (isReport) {
                // Conteúdo viola regras + foi denúncia = Denúncia APROVADA
                eventType = "report_approved";
                log.info("Denúncia de mensagem APROVADA (conteúdo viola regras). messageId={} | auctionId={} | reporterId={} | reason='{}'",
                        message.getMessageId(), message.getAuctionId(), userId, reason);
                reviewEvent = new MessageReportApproved(
                        message.getAuctionId(),
                        message.getSellerId(),
                        userId,
                        message.getMessageId(),
                        message.getMessage(),
                        reason,
                        Instant.now(),
                        UUID.randomUUID()
                );
            } else {
                //Criação normal reprovada
                eventType = "rejected";
                log.info("Mensagem REPROVADA. messageId={} | auctionId={} | sellerId={} | reason='{}'",
                        message.getMessageId(), message.getAuctionId(), message.getSellerId(), reason);
                reviewEvent = new MessageReviewRejected(
                        message.getAuctionId(),
                        message.getSellerId(),
                        message.getMessageId(),
                        reason,
                        Instant.now(),
                        UUID.randomUUID()
                );
            }
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