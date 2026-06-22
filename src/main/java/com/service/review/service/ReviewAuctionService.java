package com.service.review.service;

import com.service.review.domain.Auction;
import com.service.review.domain.ReviewAuction;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.dto.ReviewResponse;
import com.service.review.enums.ContextType;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.*;
import com.service.review.metrics.ReviewMetrics;
import com.service.review.repository.ReviewAuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAuctionService {
    private final ReviewAuctionRepository reviewAuctionRepository;
    private final ReviewService reviewService;
    private final KafkaService kafkaService;
    private final ReviewMetrics reviewMetrics;

    public ReviewResponse reviewAuction(Auction auction, ReviewAuctionContext reviewAuctionContext, UUID userId) {
        log.info("Iniciando revisão do leilão. auctionId={} | sellerId={}",
                auction.getAuctionId(), auction.getSellerId());

        ContextType contextType = reviewAuctionContext.getType();
        long start = System.currentTimeMillis();

        try {
            ReviewResponse reviewResponse = analyzeAuction(auction, reviewAuctionContext, contextType);
            persistReview(auction, reviewResponse, reviewAuctionContext, contextType);
            publishEvent(auction, reviewResponse, reviewAuctionContext, userId, contextType);

            reviewMetrics.recordProcessingTime("auction", contextType, "success", System.currentTimeMillis() - start);
            log.info("Revisão do leilão concluída. auctionId={} | approved={} | reason='{}'",
                    auction.getAuctionId(), reviewResponse.approved(), reviewResponse.repprovedReason());

            return reviewResponse;
        } catch (Exception e) {
            reviewMetrics.recordProcessingTime("auction", contextType, "failure", System.currentTimeMillis() - start);
            throw e;
        }
    }

    private ReviewResponse analyzeAuction(Auction auction, ReviewAuctionContext reviewAuctionContext, ContextType contextType) {
        log.debug("Enviando leilão para análise de IA. auctionId={} | contextId={}",
                auction.getAuctionId(), reviewAuctionContext.getId());

        byte[] imageBytes = null;
        String mimeType = null;

        if (!auction.getAuctionThumb().isBlank()) {
            try {
                URL url = new URI(auction.getAuctionThumb()).toURL();
                URLConnection connection = url.openConnection();
                mimeType = connection.getContentType();
                if (mimeType == null) mimeType = "image/jpeg";
                try (InputStream in = connection.getInputStream()) {
                    imageBytes = in.readAllBytes();
                }
            } catch (Exception e) {
                reviewMetrics.incrementImageDownloadFailure("auction", contextType);
                log.error("Erro ao baixar imagem do leilão. auctionId={} | url={} | erro={}",
                        auction.getAuctionId(), auction.getAuctionThumb(), e.getMessage());
            }
        }

        String input = buildInput(auction.toString(), auction.getReportReason(), contextType);

        ReviewResponse response = reviewService.analyze(input, reviewAuctionContext.getContext(), imageBytes, mimeType);

        if (response.approved()) {
            reviewMetrics.incrementApproved("auction", contextType);
        } else {
            reviewMetrics.incrementRejected("auction", contextType);
        }

        log.debug("Análise de IA recebida para leilão. auctionId={} | approved={} | reason='{}'",
                auction.getAuctionId(), response.approved(), response.repprovedReason());

        return response;
    }

    private String buildInput(String payload, String reportReason, ContextType type) {
        if (type == ContextType.REPORTED && reportReason != null && !reportReason.isBlank()) {
            return payload + "\n\nreport: " + reportReason;
        }
        return payload;
    }

    private void persistReview(Auction auction, ReviewResponse reviewResponse,
                               ReviewAuctionContext reviewAuctionContext, ContextType contextType) {
        log.debug("Persistindo revisão do leilão. auctionId={} | approved={}",
                auction.getAuctionId(), reviewResponse.approved());

        ReviewAuction reviewAuction = contextType == ContextType.REPORTED
                ? new ReviewAuction(auction.getAuctionId(), auction.getSellerId(), reviewResponse.approved(),
                reviewResponse.repprovedReason(), auction.getReportReason(), contextType, reviewAuctionContext)
                : new ReviewAuction(auction.getAuctionId(), auction.getSellerId(), reviewResponse.approved(),
                reviewResponse.repprovedReason(), contextType, reviewAuctionContext);

        try {
            ReviewAuction saved = reviewAuctionRepository.save(reviewAuction);
            log.info("Revisão do leilão persistida com sucesso. reviewId={} | auctionId={} | approved={}",
                    saved.getId(), auction.getAuctionId(), reviewResponse.approved());
        } catch (Exception e) {
            reviewMetrics.incrementPersistFailure("auction", contextType);
            log.error("Falha ao persistir revisão do leilão. auctionId={} | erro={}",
                    auction.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }

    private void publishEvent(Auction auction, ReviewResponse reviewResponse,
                              ReviewAuctionContext reviewAuctionContext, UUID userId, ContextType contextType) {
        boolean isReport = contextType == ContextType.REPORTED;
        ReviewEvent reviewEvent;
        String eventType;

        if (reviewResponse.approved()) {
            if (isReport) {
                eventType = "report_approved";
                log.info("Report de leilão APROVADO — publicando evento AuctionReportApproved. auctionId={} | sellerId={}",
                        auction.getAuctionId(), auction.getSellerId());
                reviewEvent = new AuctionReportApproved(auction.getAuctionId(), auction.getSellerId(), userId,
                        reviewResponse.repprovedReason(), auction.getAuctionTitle(), auction.getAuctionThumb(),
                        auction.getAuctionDescription(), Instant.now(), UUID.randomUUID());
            } else {
                eventType = "approved";
                log.info("Leilão APROVADO — publicando evento AuctionReviewApproved. auctionId={} | sellerId={}",
                        auction.getAuctionId(), auction.getSellerId());
                reviewEvent = new AuctionReviewApproved(auction.getAuctionId(), auction.getSellerId(),
                        Instant.now(), UUID.randomUUID());
            }
        } else {
            eventType = "rejected";
            log.info("Leilão REPROVADO — publicando evento AuctionReviewRejected. auctionId={} | sellerId={} | reason='{}'",
                    auction.getAuctionId(), auction.getSellerId(), reviewResponse.repprovedReason());
            reviewEvent = new AuctionReviewRejected(auction.getAuctionId(), auction.getSellerId(),
                    reviewResponse.repprovedReason(), Instant.now(), UUID.randomUUID());
        }

        try {
            kafkaService.sendEvent(reviewEvent);
            reviewMetrics.incrementEventPublished("auction", contextType, eventType);
            log.debug("Evento de revisão publicado no Kafka. tipo={} | auctionId={}",
                    reviewEvent.getClass().getSimpleName(), auction.getAuctionId());
        } catch (Exception e) {
            reviewMetrics.incrementEventPublishFailure("auction", contextType, eventType);
            log.error("Falha ao publicar evento no Kafka. tipo={} | auctionId={} | erro={}",
                    reviewEvent.getClass().getSimpleName(), auction.getAuctionId(), e.getMessage(), e);
            throw e;
        }
    }
}