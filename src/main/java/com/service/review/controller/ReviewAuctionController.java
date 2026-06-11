package com.service.review.controller;

import com.service.review.domain.ReviewAuction;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.dto.ReviewAuctionRequest;
import com.service.review.exception.ResourceNotFoundException;
import com.service.review.repository.ReviewAuctionContextRepository;
import com.service.review.repository.ReviewAuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/review-auctions")
@RequiredArgsConstructor
public class ReviewAuctionController {
    private final ReviewAuctionRepository reviewAuctionRepository;
    private final ReviewAuctionContextRepository reviewAuctionContextRepository;

    @GetMapping
    public ResponseEntity<List<ReviewAuction>> getAll() {
        log.debug("GET /api/v1/review-auctions — buscando todas as revisoes de leilao");
        List<ReviewAuction> result = reviewAuctionRepository.findAll();
        log.debug("Total de revisoes de leilao encontradas: {}", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewAuction> getById(@PathVariable Long id) {
        log.debug("GET /api/v1/review-auctions/{} — buscando revisao por ID", id);
        ReviewAuction reviewAuction = reviewAuctionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewAuction nao encontrada. id={}", id);
                    return new ResourceNotFoundException("ReviewAuction not found with id: " + id);
                });
        log.debug("ReviewAuction encontrada. id={} | auctionId={} | approved={}",
                id, reviewAuction.getAuctionId(), reviewAuction.getApproved());
        return ResponseEntity.ok(reviewAuction);
    }

    @GetMapping("/by-auction/{auctionId}")
    public ResponseEntity<List<ReviewAuction>> getAllByAuctionId(@PathVariable Long auctionId) {
        log.debug("GET /api/v1/review-auctions/by-auction/{} — buscando revisoes por leilao", auctionId);
        List<ReviewAuction> result = reviewAuctionRepository.findByAuctionId(auctionId);
        log.debug("Revisoes encontradas para auctionId={}: {}", auctionId, result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-seller/{sellerId}")
    public ResponseEntity<List<ReviewAuction>> getAllBySellerId(@PathVariable UUID sellerId) {
        log.debug("GET /api/v1/review-auctions/by-seller/{} — buscando revisoes por vendedor", sellerId);
        List<ReviewAuction> result = reviewAuctionRepository.findBySellerId(sellerId);
        log.debug("Revisoes encontradas para sellerId={}: {}", sellerId, result.size());
        return ResponseEntity.ok(result);
    }
}
