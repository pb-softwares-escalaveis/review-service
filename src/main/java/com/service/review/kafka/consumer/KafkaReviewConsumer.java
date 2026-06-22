package com.service.review.kafka.consumer;

import com.service.review.domain.Auction;
import com.service.review.domain.Message;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.enums.ContextType;
import com.service.review.exception.ReviewContextNotFoundException;
import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.kafka.events.AuctionReportedPendingReview;
import com.service.review.kafka.events.MessageCreatedPendingReview;
import com.service.review.kafka.events.MessageReportedPendingReview;
import com.service.review.metrics.ConsumerMetrics;
import com.service.review.service.ReviewAuctionContextService;
import com.service.review.service.ReviewAuctionService;
import com.service.review.service.ReviewMessageContextService;
import com.service.review.service.ReviewMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReviewConsumer {
    private final ReviewAuctionService reviewAuctionService;
    private final ReviewAuctionContextService reviewAuctionContextService;
    private final ReviewMessageService reviewMessageService;
    private final ReviewMessageContextService reviewMessageContextService;
    private final ConsumerMetrics consumerMetrics;

    @KafkaListener(topics = "auctions.lot.created-pending")
    public void consumeAuctionPendingReview(AuctionCreatedPendingReview event) {
        log.info("Evento recebido do Kafka [auctions.lot.created-pending]. auctionId={} | sellerId={}",
                event.auctionId(), event.sellerId());

        consumerMetrics.incrementReceived("auction", "created");

        Auction auction = Auction.from(event);
        log.debug("Evento mapeado para domínio Auction. auctionId={}", auction.getAuctionId());

        ReviewAuctionContext reviewAuctionContext = reviewAuctionContextService.getContext(ContextType.CREATED);

        if (reviewAuctionContext == null) {
            log.error("Processamento abortado — nenhum ReviewAuctionContext disponível. " +
                    "Cadastre um contexto antes de processar revisões. auctionId={}", auction.getAuctionId());
            consumerMetrics.incrementContextNotFound("auction", "created");
            consumerMetrics.incrementFailed("auction", "created", "context_not_found");
            throw new ReviewContextNotFoundException("ReviewAuctionContext", ContextType.CREATED);
        }

        log.debug("Contexto de revisão carregado. contextId={} | auctionId={}",
                reviewAuctionContext.getId(), auction.getAuctionId());

        long start = System.currentTimeMillis();
        try {
            reviewAuctionService.reviewAuction(auction, reviewAuctionContext, event.sellerId());
            consumerMetrics.incrementProcessed("auction", "created");
            consumerMetrics.recordProcessingTime("auction", "created", "success",
                    System.currentTimeMillis() - start);
            log.info("Processamento do evento de leilão finalizado com sucesso. auctionId={}", auction.getAuctionId());
        } catch (Exception e) {
            consumerMetrics.incrementFailed("auction", "created", "processing_error");
            consumerMetrics.recordProcessingTime("auction", "created", "failure",
                    System.currentTimeMillis() - start);
            log.error("Erro ao processar revisão do leilão. auctionId={} | erro={}",
                    auction.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "qa.review.created-pending")
    public void consumeMessagePendingReview(MessageCreatedPendingReview event) {
        log.info("Evento recebido do Kafka [qa.created-pending]. auctionId={} | messageId={} | sellerId={}",
                event.auctionId(), event.messageId(), event.sellerId());

        consumerMetrics.incrementReceived("auction", "reported");

        Message message = Message.from(event);
        log.debug("Evento mapeado para domínio Message. messageId={} | auctionId={}",
                message.getMessageId(), message.getAuctionId());

        ReviewMessageContext reviewMessageContext = reviewMessageContextService.getContext(ContextType.CREATED);

        if (reviewMessageContext == null) {
            log.error("Processamento abortado — nenhum ReviewMessageContext disponível. " +
                            "Cadastre um contexto antes de processar revisões. messageId={} | auctionId={}",
                    message.getMessageId(), message.getAuctionId());
            consumerMetrics.incrementContextNotFound("message", "created");
            consumerMetrics.incrementFailed("message", "created", "context_not_found");
            throw new ReviewContextNotFoundException("ReviewMessageContext", ContextType.CREATED);
        }

        log.debug("Contexto de revisão carregado. contextId={} | messageId={}",
                reviewMessageContext.getId(), message.getMessageId());

        long start = System.currentTimeMillis();
        try {
            reviewMessageService.reviewMessage(message, reviewMessageContext, event.userId());
            consumerMetrics.incrementProcessed("message", "created");
            consumerMetrics.recordProcessingTime(
                    "message",
                    "created",
                    "success",
                    System.currentTimeMillis() - start
            );
            log.info("Processamento do evento de mensagem finalizado com sucesso. messageId={} | auctionId={}",
                    message.getMessageId(), message.getAuctionId());
        } catch (Exception e) {
            consumerMetrics.incrementFailed("message", "created", "processing_error");
            consumerMetrics.recordProcessingTime(
                    "message",
                    "created",
                    "failure",
                    System.currentTimeMillis() - start
            );
            log.error("Erro ao processar revisão da mensagem. messageId={} | auctionId={} | erro={}",
                    message.getMessageId(), message.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "reports.auction.pending-review")
    public void consumeAuctionPendingReportReview(AuctionReportedPendingReview event) {
        log.info("Evento recebido do Kafka [report.auction-reported]. auctionId={} | sellerId={} | userId={}",
                event.auctionId(), event.sellerId(), event.userId());

        consumerMetrics.incrementReceived("auction", "reported");

        Auction auction = Auction.from(event);
        log.debug("Evento mapeado para domínio Auction. auctionId={}", auction.getAuctionId());

        ReviewAuctionContext reviewAuctionContext = reviewAuctionContextService.getContext(ContextType.REPORTED);

        if (reviewAuctionContext == null) {
            log.error("Processamento abortado — nenhum ReviewAuctionContext disponível para type=REPORTED. " +
                    "Cadastre um contexto antes de processar revisões. auctionId={}", auction.getAuctionId());
            consumerMetrics.incrementContextNotFound("auction", "reported");
            consumerMetrics.incrementFailed("auction", "reported", "context_not_found");
            throw new ReviewContextNotFoundException("ReviewAuctionContext", ContextType.REPORTED);
        }

        log.debug("Contexto de revisão carregado. contextId={} | auctionId={}",
                reviewAuctionContext.getId(), auction.getAuctionId());

        long start = System.currentTimeMillis();
        try {
            reviewAuctionService.reviewAuction(auction, reviewAuctionContext, event.userId());
            consumerMetrics.incrementProcessed("auction", "reported");
            consumerMetrics.recordProcessingTime(
                    "auction",
                    "reported",
                    "success",
                    System.currentTimeMillis() - start
            );
            log.info("Processamento do evento de report finalizado com sucesso. auctionId={}",
                    auction.getAuctionId());
        } catch (Exception e) {
            consumerMetrics.incrementFailed("auction", "reported", "processing_error");
            consumerMetrics.recordProcessingTime(
                    "auction",
                    "reported",
                    "failure",
                    System.currentTimeMillis() - start
            );
            log.error("Erro ao processar revisão do report. auctionId={} | erro={}",
                    auction.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "reports.qa.pending-review")
    public void consumeMessagePendingReportReview(MessageReportedPendingReview event) {
        log.info("Evento recebido do Kafka [report.message-reported]. auctionId={} | messageId={} | sellerId={} | userId={}",
                event.auctionId(), event.messageId(), event.sellerId(), event.userId());

        consumerMetrics.incrementReceived("message", "reported");

        Message message = Message.from(event);
        log.debug("Evento mapeado para domínio Message. messageId={} | auctionId={}",
                message.getMessageId(), message.getAuctionId());

        ReviewMessageContext reviewMessageContext = reviewMessageContextService.getContext(ContextType.REPORTED);

        if (reviewMessageContext == null) {
            log.error("Processamento abortado — nenhum ReviewMessageContext disponível para type=REPORTED. " +
                            "Cadastre um contexto antes de processar revisões. messageId={} | auctionId={}",
                    message.getMessageId(), message.getAuctionId());
            consumerMetrics.incrementContextNotFound("message", "reported");
            consumerMetrics.incrementFailed("message", "reported", "context_not_found");
            throw new ReviewContextNotFoundException("ReviewMessageContext", ContextType.REPORTED);
        }

        log.debug("Contexto de revisão carregado. contextId={} | messageId={}",
                reviewMessageContext.getId(), message.getMessageId());

        long start = System.currentTimeMillis();
        try {
            reviewMessageService.reviewMessage(message, reviewMessageContext, event.userId());
            consumerMetrics.incrementProcessed("message", "reported");
            consumerMetrics.recordProcessingTime(
                    "message",
                    "reported",
                    "success",
                    System.currentTimeMillis() - start
            );
            log.info("Processamento do evento de report finalizado com sucesso. messageId={} | auctionId={}",
                    message.getMessageId(), message.getAuctionId());
        } catch (Exception e) {
            consumerMetrics.incrementFailed("message", "reported", "processing_error");
            consumerMetrics.recordProcessingTime(
                    "message",
                    "reported",
                    "failure",
                    System.currentTimeMillis() - start
            );
            log.error("Erro ao processar revisão do report da mensagem. messageId={} | auctionId={} | erro={}",
                    message.getMessageId(), message.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }
}