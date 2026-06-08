package com.service.review.kafka.consumer;

import com.service.review.domain.Auction;
import com.service.review.domain.Message;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.kafka.events.AuctionCreatedPendingReview;
import com.service.review.kafka.events.MessageCreatedPendingReview;
import com.service.review.service.ReviewAuctionContextService;
import com.service.review.service.ReviewAuctionService;
import com.service.review.service.ReviewMessageContextService;
import com.service.review.service.ReviewMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @KafkaListener(topics = "auctions.lot.created-pending")
    public void consumeAuctionPendingReview(AuctionCreatedPendingReview event) {
        log.info("Evento recebido do Kafka [auctions.lot.created-pending]. auctionId={} | sellerId={}",
                event.auctionId(), event.sellerId());

        Auction auction = Auction.from(event);
        log.debug("Evento mapeado para domínio Auction. auctionId={}", auction.getAuctionId());

        ReviewAuctionContext reviewAuctionContext = reviewAuctionContextService.getLastVersionOfAuctionReviewContext();

        if (reviewAuctionContext == null) {
            log.error("Processamento abortado — nenhum ReviewAuctionContext disponível. " +
                    "Cadastre um contexto antes de processar revisões. auctionId={}", auction.getAuctionId());
            return;
        }

        log.debug("Contexto de revisão carregado. contextId={} | auctionId={}",
                reviewAuctionContext.getId(), auction.getAuctionId());

        try {
            reviewAuctionService.reviewAuction(auction, reviewAuctionContext);
            log.info("Processamento do evento de leilão finalizado com sucesso. auctionId={}", auction.getAuctionId());
        } catch (Exception e) {
            log.error("Erro ao processar revisão do leilão. auctionId={} | erro={}",
                    auction.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "qa.created-pending")
    public void consumeMessagePendingReview(MessageCreatedPendingReview event) {
        log.info("Evento recebido do Kafka [qa.created-pending]. auctionId={} | messageId={} | sellerId={}",
                event.auctionId(), event.messageId(), event.sellerId());

        Message message = Message.from(event);
        log.debug("Evento mapeado para domínio Message. messageId={} | auctionId={}",
                message.getMessageId(), message.getAuctionId());

        ReviewMessageContext reviewMessageContext = reviewMessageContextService.getLastVersionOfMessageReviewContext();

        if (reviewMessageContext == null) {
            log.error("Processamento abortado — nenhum ReviewMessageContext disponível. " +
                            "Cadastre um contexto antes de processar revisões. messageId={} | auctionId={}",
                    message.getMessageId(), message.getAuctionId());
            return;
        }

        log.debug("Contexto de revisão carregado. contextId={} | messageId={}",
                reviewMessageContext.getId(), message.getMessageId());

        try {
            reviewMessageService.reviewMessage(message, reviewMessageContext);
            log.info("Processamento do evento de mensagem finalizado com sucesso. messageId={} | auctionId={}",
                    message.getMessageId(), message.getAuctionId());
        } catch (Exception e) {
            log.error("Erro ao processar revisão da mensagem. messageId={} | auctionId={} | erro={}",
                    message.getMessageId(), message.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }
}