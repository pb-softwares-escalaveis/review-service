package com.service.review.service;

import com.service.review.domain.Message;
import com.service.review.domain.ReviewMessage;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.dto.ReviewResponse;
import com.service.review.enums.ContextType;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.*;
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

    public ReviewResponse reviewMessage(Message message, ReviewMessageContext reviewMessageContext) {
        log.info("Iniciando revisão da mensagem. auctionId={} | messageId={} | sellerId={}",
                message.getAuctionId(), message.getMessageId(), message.getSellerId());

        ReviewResponse reviewResponse = analyzeMessage(message, reviewMessageContext);
        persistReview(message, reviewMessageContext, reviewResponse);
        publishEvent(message, reviewResponse, reviewMessageContext);

        log.info("Revisão da mensagem concluída. auctionId={} | messageId={} | approved={} | reason='{}'",
                message.getAuctionId(), message.getMessageId(),
                reviewResponse.approved(), reviewResponse.repprovedReason());

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
                message.getMessageId(), response.approved(), response.repprovedReason());

        return response;
    }

    private void persistReview(Message message, ReviewMessageContext reviewMessageContext, ReviewResponse reviewResponse) {
        log.debug("Persistindo revisão da mensagem. messageId={} | auctionId={} | approved={}",
                message.getMessageId(), message.getAuctionId(), reviewResponse.approved());

        ReviewMessage reviewMessage;
        boolean isReport = reviewMessageContext.getType() == ContextType.REPORTED;

        if (isReport) {
            reviewMessage = new ReviewMessage(
                    message.getAuctionId(),
                    message.getSellerId(),
                    message.getMessageId(),
                    reviewResponse.approved(),
                    reviewResponse.repprovedReason(),
                    message.getReportReason(),
                    reviewMessageContext.getType(),
                    reviewMessageContext
            );
        } else {
            reviewMessage = new ReviewMessage(
                    message.getAuctionId(),
                    message.getSellerId(),
                    message.getMessageId(),
                    reviewResponse.approved(),
                    reviewResponse.repprovedReason(),
                    reviewMessageContext
            );
        }

        ReviewMessage saved = reviewMessageRepository.save(reviewMessage);
        log.info("Revisão da mensagem persistida com sucesso. reviewId={} | messageId={} | auctionId={} | approved={}",
                saved.getId(), message.getMessageId(), message.getAuctionId(), reviewResponse.approved());
    }

    private void publishEvent(Message message, ReviewResponse reviewResponse, ReviewMessageContext reviewMessageContext) {
        ReviewEvent reviewEvent;
        boolean isReport = reviewMessageContext.getType() == ContextType.REPORTED;

        if (reviewResponse.approved()) {
            if (isReport) {
                log.info("Report de mensagem APROVADO — publicando evento MessageReportApproved. messageId={} | auctionId={} | sellerId={}",
                        message.getMessageId(), message.getAuctionId(), message.getSellerId());
                reviewEvent = new MessageReportApproved(
                        message.getUserId(),
                        message.getSellerId(),
                        message.getAuctionId(),
                        message.getMessageId(),
                        message.getMessage(),
                        message.getReportReason(),
                        reviewResponse.repprovedReason(),
                        Instant.now(),
                        UUID.randomUUID()
                );
            } else {
                log.info("Mensagem APROVADA — publicando evento MessageReviewApproved. messageId={} | auctionId={} | sellerId={}",
                        message.getMessageId(), message.getAuctionId(), message.getSellerId());
                reviewEvent = new MessageReviewApproved(
                        message.getUserId(),
                        message.getAuctionId(),
                        message.getSellerId(),
                        message.getMessageId(),
                        Instant.now(),
                        UUID.randomUUID()
                );
            }
        } else {
            log.info("Mensagem REPROVADA — publicando evento MessageReviewRejected. messageId={} | auctionId={} | sellerId={} | reason='{}'",
                    message.getMessageId(), message.getAuctionId(),
                    message.getSellerId(), reviewResponse.repprovedReason());
            reviewEvent = new MessageReviewRejected(
                    message.getAuctionId(),
                    message.getSellerId(),
                    message.getMessageId(),
                    reviewResponse.repprovedReason(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        }

        kafkaService.sendEvent(reviewEvent);
        log.debug("Evento de revisão publicado no Kafka. tipo={} | messageId={} | auctionId={}",
                reviewEvent.getClass().getSimpleName(), message.getMessageId(), message.getAuctionId());
    }
}