package com.service.review.service;

import com.service.review.domain.Auction;
import com.service.review.domain.ReviewAuction;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.dto.ReviewResponse;
import com.service.review.kafka.KafkaService;
import com.service.review.kafka.events.AuctionReviewApproved;
import com.service.review.kafka.events.AuctionReviewRejected;
import com.service.review.kafka.events.ReviewEvent;
import com.service.review.repository.ReviewAuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public ReviewResponse reviewAuction(Auction auction, ReviewAuctionContext reviewAuctionContext) {
        log.info("Iniciando revisão do leilão. auctionId={} | sellerId={}",
                auction.getAuctionId(), auction.getSellerId());

        ReviewResponse reviewResponse = analyzeAuction(auction, reviewAuctionContext);
        persistReview(auction, reviewResponse, reviewAuctionContext);
        publishEvent(auction, reviewResponse);

        log.info("Revisão do leilão concluída. auctionId={} | approved={} | reason='{}'",
                auction.getAuctionId(), reviewResponse.approved(), reviewResponse.reason());

        return reviewResponse;
    }

    private ReviewResponse analyzeAuction(Auction auction, ReviewAuctionContext reviewAuctionContext) {
        log.debug("Enviando leilão para análise de IA. auctionId={} | contextId={}",
                auction.getAuctionId(), reviewAuctionContext.getId());

        String thumbStr = auction.getAuctionThumb();
        if (!thumbStr.startsWith("http://") && !thumbStr.startsWith("https://")) {
            thumbStr = "https://" + thumbStr;
        }
        byte[] imageBytes = null;
        String mimeType = null;

        if (thumbStr.isBlank()) {
            try {

                URL url = new URI(auction.getAuctionThumb()).toURL();
                URLConnection connection = url.openConnection();
                mimeType = connection.getContentType();

                if (mimeType == null) {
                    mimeType = "image/jpeg";
                }

                try (InputStream in = connection.getInputStream()) {
                    imageBytes = in.readAllBytes();
                }
            } catch (Exception e) {
                log.error("Erro ao baixar imagem do leilão. auctionId={} | url={} | erro={}",
                        auction.getAuctionId(), auction.getAuctionThumb(), e.getMessage());
            }
        }

        ReviewResponse response = reviewService.analyze(
                auction.toString(),
                reviewAuctionContext.getContext(),
                imageBytes,
                mimeType
        );

        log.debug("Análise de IA recebida para leilão. auctionId={} | approved={} | reason='{}'",
                auction.getAuctionId(), response.approved(), response.reason());

        return response;
    }

    private void persistReview(Auction auction, ReviewResponse reviewResponse, ReviewAuctionContext reviewAuctionContext) {
        log.debug("Persistindo revisão do leilão. auctionId={} | approved={}",
                auction.getAuctionId(), reviewResponse.approved());

        ReviewAuction reviewAuction = new ReviewAuction(
                auction.getAuctionId(),
                auction.getSellerId(),
                reviewResponse.approved(),
                reviewResponse.reason(),
                reviewAuctionContext
        );

        ReviewAuction saved = reviewAuctionRepository.save(reviewAuction);
        log.info("Revisão do leilão persistida com sucesso. reviewId={} | auctionId={} | approved={}",
                saved.getId(), auction.getAuctionId(), reviewResponse.approved());
    }

    private void publishEvent(Auction auction, ReviewResponse reviewResponse) {
        ReviewEvent reviewEvent;

        if (reviewResponse.approved()) {
            log.info("Leilão APROVADO — publicando evento AuctionReviewApproved. auctionId={} | sellerId={}",
                    auction.getAuctionId(), auction.getSellerId());
            reviewEvent = new AuctionReviewApproved(
                    auction.getAuctionId(),
                    auction.getSellerId(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        } else {
            log.info("Leilão REPROVADO — publicando evento AuctionReviewRejected. auctionId={} | sellerId={} | reason='{}'",
                    auction.getAuctionId(), auction.getSellerId(), reviewResponse.reason());
            reviewEvent = new AuctionReviewRejected(
                    auction.getAuctionId(),
                    auction.getSellerId(),
                    reviewResponse.reason(),
                    Instant.now(),
                    UUID.randomUUID()
            );
        }

        kafkaService.sendEvent(reviewEvent);
        log.debug("Evento de revisão publicado no Kafka. tipo={} | auctionId={}",
                reviewEvent.getClass().getSimpleName(), auction.getAuctionId());
    }
}