package com.service.review.controller;

import com.service.review.domain.ReviewMessage;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.dto.ReviewAuctionRequest;
import com.service.review.dto.ReviewMessageRequest;
import com.service.review.repository.ReviewMessageContextRepository;
import com.service.review.repository.ReviewMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/review-messages")
@RequiredArgsConstructor
public class ReviewMessageController {
    private final ReviewMessageRepository reviewMessageRepository;
    private final ReviewMessageContextRepository reviewMessageContextRepository;

    @GetMapping
    public ResponseEntity<List<ReviewMessage>> getAll() {
        return ResponseEntity.ok(reviewMessageRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewMessage> getById(@PathVariable Long id) {
        ReviewMessage reviewMessage = reviewMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewMessage not found with id: " + id));
        return ResponseEntity.ok(reviewMessage);
    }

    @GetMapping("/by-auction/{auctionId}")
    public ResponseEntity<List<ReviewMessage>> getAllByAuctionId(@PathVariable Long auctionId) {
        return ResponseEntity.ok(reviewMessageRepository.findByAuctionId(auctionId));
    }

    @GetMapping("/by-seller/{sellerId}")
    public ResponseEntity<List<ReviewMessage>> getAllBySellerId(@PathVariable UUID sellerId) {
        return ResponseEntity.ok(reviewMessageRepository.findBySellerId(sellerId));
    }

    @GetMapping("/by-message/{messageId}")
    public ResponseEntity<List<ReviewMessage>> getAllByMessageId(@PathVariable Long messageId) {
        return ResponseEntity.ok(reviewMessageRepository.findByMessageId(messageId));
    }

    @PostMapping
    public ResponseEntity<ReviewMessage> create(@RequestBody ReviewMessageRequest request) {
        ReviewMessageContext context = reviewMessageContextRepository.findTopByOrderByIdDesc();
        if (context == null) {
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewMessageRepository.save(reviewMessage));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewMessage> update(@PathVariable Long id,
                                                @RequestBody ReviewMessageRequest request) {
        ReviewMessage reviewMessage = reviewMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewMessage not found with id: " + id));

        reviewMessage.setAuctionId(request.auctionId());
        reviewMessage.setSellerId(request.sellerId());
        reviewMessage.setMessageId(request.messageId());
        reviewMessage.setApproved(request.approved());
        reviewMessage.setReason(request.reason());

        if (request.contextId() != null) {
            ReviewMessageContext context = reviewMessageContextRepository.findById(request.contextId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "ReviewMessageContext not found with id: " + request.contextId()));
            reviewMessage.setReviewMessageContext(context);
        }

        return ResponseEntity.ok(reviewMessageRepository.save(reviewMessage));
    }

    @PatchMapping("/{id}/approval")
    public ResponseEntity<ReviewMessage> patchApproval(@PathVariable Long id,
                                                       @RequestParam Boolean approved,
                                                       @RequestParam(required = false) String reason) {
        ReviewMessage reviewMessage = reviewMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewMessage not found with id: " + id));

        reviewMessage.setApproved(approved);
        if (reason != null) reviewMessage.setReason(reason);

        return ResponseEntity.ok(reviewMessageRepository.save(reviewMessage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!reviewMessageRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ReviewMessage not found with id: " + id);
        }
        reviewMessageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}