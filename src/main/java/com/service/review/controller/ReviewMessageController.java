package com.service.review.controller;

import com.service.review.domain.ReviewMessage;
import com.service.review.exception.ResourceNotFoundException;
import com.service.review.repository.ReviewMessageContextRepository;
import com.service.review.repository.ReviewMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/review-message")
@RequiredArgsConstructor
public class ReviewMessageController {
    private final ReviewMessageRepository reviewMessageRepository;

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
                    return new ResourceNotFoundException("ReviewMessage not found with id: " + id);
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
}
