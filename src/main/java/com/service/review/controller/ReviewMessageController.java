package com.service.review.controller;

import com.service.review.domain.ReviewMessage;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.dto.ReviewMessageRequest;
import com.service.review.repository.ReviewMessageContextRepository;
import com.service.review.repository.ReviewMessageRepository;
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
@RequestMapping("/api/v1/review-messages")
@RequiredArgsConstructor
public class ReviewMessageController {
    private final ReviewMessageRepository reviewMessageRepository;
    private final ReviewMessageContextRepository reviewMessageContextRepository;

    @GetMapping
    public ResponseEntity<List<ReviewMessage>> getAll() {
        log.debug("GET /api/v1/review-messages — buscando todas as revisoes de mensagem");
        List<ReviewMessage> result = reviewMessageRepository.findAll();
        log.debug("Total de revisoes de mensagem encontradas: {}", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewMessage> getById(@PathVariable Long id) {
        log.debug("GET /api/v1/review-messages/{} — buscando revisao por ID", id);
        ReviewMessage reviewMessage = reviewMessageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewMessage nao encontrada. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewMessage not found with id: " + id);
                });
        log.debug("ReviewMessage encontrada. id={} | messageId={} | auctionId={} | approved={}",
                id, reviewMessage.getMessageId(), reviewMessage.getAuctionId(), reviewMessage.getApproved());
        return ResponseEntity.ok(reviewMessage);
    }

    @GetMapping("/by-auction/{auctionId}")
    public ResponseEntity<List<ReviewMessage>> getAllByAuctionId(@PathVariable Long auctionId) {
        log.debug("GET /api/v1/review-messages/by-auction/{} — buscando revisoes por leilao", auctionId);
        List<ReviewMessage> result = reviewMessageRepository.findByAuctionId(auctionId);
        log.debug("Revisoes encontradas para auctionId={}: {}", auctionId, result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-seller/{sellerId}")
    public ResponseEntity<List<ReviewMessage>> getAllBySellerId(@PathVariable UUID sellerId) {
        log.debug("GET /api/v1/review-messages/by-seller/{} — buscando revisoes por vendedor", sellerId);
        List<ReviewMessage> result = reviewMessageRepository.findBySellerId(sellerId);
        log.debug("Revisoes encontradas para sellerId={}: {}", sellerId, result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-message/{messageId}")
    public ResponseEntity<List<ReviewMessage>> getAllByMessageId(@PathVariable Long messageId) {
        log.debug("GET /api/v1/review-messages/by-message/{} — buscando revisoes por mensagem", messageId);
        List<ReviewMessage> result = reviewMessageRepository.findByMessageId(messageId);
        log.debug("Revisoes encontradas para messageId={}: {}", messageId, result.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ReviewMessage> create(@RequestBody ReviewMessageRequest request) {
        log.info("POST /api/v1/review-messages — criando revisao manual. messageId={} | auctionId={} | sellerId={} | approved={}",
                request.messageId(), request.auctionId(), request.sellerId(), request.approved());

        ReviewMessageContext context = reviewMessageContextRepository.findTopByOrderByIdDesc();
        if (context == null) {
            log.error("Nenhum ReviewMessageContext disponivel. messageId={} | auctionId={}",
                    request.messageId(), request.auctionId());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No ReviewMessageContext available. Please create one first.");
        }

        ReviewMessage reviewMessage = new ReviewMessage(
                request.auctionId(),
                request.sellerId(),
                request.messageId(),
                request.approved(),
                request.reason(),
                context
        );

        ReviewMessage saved = reviewMessageRepository.save(reviewMessage);
        log.info("ReviewMessage criada com sucesso. reviewId={} | messageId={} | auctionId={} | approved={}",
                saved.getId(), saved.getMessageId(), saved.getAuctionId(), saved.getApproved());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewMessage> update(@PathVariable Long id,
                                                @RequestBody ReviewMessageRequest request) {
        log.info("PUT /api/v1/review-messages/{} — atualizando revisao de mensagem", id);

        ReviewMessage reviewMessage = reviewMessageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewMessage nao encontrada para atualizacao. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewMessage not found with id: " + id);
                });

        reviewMessage.setAuctionId(request.auctionId());
        reviewMessage.setSellerId(request.sellerId());
        reviewMessage.setMessageId(request.messageId());
        reviewMessage.setApproved(request.approved());
        reviewMessage.setReason(request.reason());

        if (request.contextId() != null) {
            log.debug("Atualizando contexto da revisao. reviewId={} | novoContextId={}", id, request.contextId());
            ReviewMessageContext context = reviewMessageContextRepository.findById(request.contextId())
                    .orElseThrow(() -> {
                        log.warn("ReviewMessageContext nao encontrado. contextId={}", request.contextId());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "ReviewMessageContext not found with id: " + request.contextId());
                    });
            reviewMessage.setReviewMessageContext(context);
        }

        ReviewMessage saved = reviewMessageRepository.save(reviewMessage);
        log.info("ReviewMessage atualizada com sucesso. reviewId={} | messageId={} | auctionId={} | approved={}",
                saved.getId(), saved.getMessageId(), saved.getAuctionId(), saved.getApproved());

        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/approval")
    public ResponseEntity<ReviewMessage> patchApproval(@PathVariable Long id,
                                                       @RequestParam Boolean approved,
                                                       @RequestParam(required = false) String reason) {
        log.info("PATCH /api/v1/review-messages/{}/approval — approved={} | reason='{}'", id, approved, reason);

        ReviewMessage reviewMessage = reviewMessageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewMessage nao encontrada para patch. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewMessage not found with id: " + id);
                });

        reviewMessage.setApproved(approved);
        if (reason != null) reviewMessage.setReason(reason);

        ReviewMessage saved = reviewMessageRepository.save(reviewMessage);
        log.info("Aprovacao atualizada com sucesso. reviewId={} | messageId={} | approved={}",
                saved.getId(), saved.getMessageId(), saved.getApproved());

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/review-messages/{} — removendo revisao de mensagem", id);

        if (!reviewMessageRepository.existsById(id)) {
            log.warn("ReviewMessage nao encontrada para exclusao. id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ReviewMessage not found with id: " + id);
        }

        reviewMessageRepository.deleteById(id);
        log.info("ReviewMessage removida com sucesso. id={}", id);

        return ResponseEntity.noContent().build();
    }
}