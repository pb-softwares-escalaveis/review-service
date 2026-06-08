package com.service.review.controller;

import com.service.review.domain.ReviewAuction;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.dto.ReviewAuctionRequest;
import com.service.review.repository.ReviewAuctionContextRepository;
import com.service.review.repository.ReviewAuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewAuction not found with id: " + id);
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

    @PostMapping
    public ResponseEntity<ReviewAuction> create(@RequestBody ReviewAuctionRequest request) {
        log.info("POST /api/v1/review-auctions — criando revisao manual. auctionId={} | sellerId={} | approved={}",
                request.auctionId(), request.sellerId(), request.approved());

        ReviewAuctionContext context = reviewAuctionContextRepository.findTopByOrderByIdDesc();
        if (context == null) {
            log.error("Nenhum ReviewAuctionContext disponivel. auctionId={}", request.auctionId());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No ReviewAuctionContext available. Please create one first.");
        }

        ReviewAuction reviewAuction = new ReviewAuction(
                request.auctionId(),
                request.sellerId(),
                request.approved(),
                request.reason(),
                context
        );

        ReviewAuction saved = reviewAuctionRepository.save(reviewAuction);
        log.info("ReviewAuction criada com sucesso. reviewId={} | auctionId={} | approved={}",
                saved.getId(), saved.getAuctionId(), saved.getApproved());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewAuction> update(@PathVariable Long id,
                                                @RequestBody ReviewAuctionRequest request) {
        log.info("PUT /api/v1/review-auctions/{} — atualizando revisao de leilao", id);

        ReviewAuction reviewAuction = reviewAuctionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewAuction nao encontrada para atualizacao. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewAuction not found with id: " + id);
                });

        reviewAuction.setAuctionId(request.auctionId());
        reviewAuction.setSellerId(request.sellerId());
        reviewAuction.setApproved(request.approved());
        reviewAuction.setReason(request.reason());

        if (request.contextId() != null) {
            log.debug("Atualizando contexto da revisao. reviewId={} | novoContextId={}", id, request.contextId());
            ReviewAuctionContext context = reviewAuctionContextRepository.findById(request.contextId())
                    .orElseThrow(() -> {
                        log.warn("ReviewAuctionContext nao encontrado. contextId={}", request.contextId());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "ReviewAuctionContext not found with id: " + request.contextId());
                    });
            reviewAuction.setReviewAuctionContext(context);
        }

        ReviewAuction saved = reviewAuctionRepository.save(reviewAuction);
        log.info("ReviewAuction atualizada com sucesso. reviewId={} | auctionId={} | approved={}",
                saved.getId(), saved.getAuctionId(), saved.getApproved());

        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/approval")
    public ResponseEntity<ReviewAuction> patchApproval(@PathVariable Long id,
                                                       @RequestParam Boolean approved,
                                                       @RequestParam(required = false) String reason) {
        log.info("PATCH /api/v1/review-auctions/{}/approval — approved={} | reason='{}'", id, approved, reason);

        ReviewAuction reviewAuction = reviewAuctionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewAuction nao encontrada para patch. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewAuction not found with id: " + id);
                });

        reviewAuction.setApproved(approved);
        if (reason != null) reviewAuction.setReason(reason);

        ReviewAuction saved = reviewAuctionRepository.save(reviewAuction);
        log.info("Aprovacao atualizada com sucesso. reviewId={} | auctionId={} | approved={}",
                saved.getId(), saved.getAuctionId(), saved.getApproved());

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/review-auctions/{} — removendo revisao de leilao", id);

        if (!reviewAuctionRepository.existsById(id)) {
            log.warn("ReviewAuction nao encontrada para exclusao. id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ReviewAuction not found with id: " + id);
        }

        reviewAuctionRepository.deleteById(id);
        log.info("ReviewAuction removida com sucesso. id={}", id);

        return ResponseEntity.noContent().build();
    }
}