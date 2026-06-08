package com.service.review.controller;

import com.service.review.domain.ReviewAuction;
import com.service.review.domain.ReviewAuctionContext;
import com.service.review.dto.ReviewAuctionRequest;
import com.service.review.repository.ReviewAuctionContextRepository;
import com.service.review.repository.ReviewAuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/review-auctions")
@RequiredArgsConstructor
public class ReviewAuctionController {
    private final ReviewAuctionRepository reviewAuctionRepository;
    private final ReviewAuctionContextRepository reviewAuctionContextRepository;

    @GetMapping
    public ResponseEntity<List<ReviewAuction>> getAll() {
        return ResponseEntity.ok(reviewAuctionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewAuction> getById(@PathVariable Long id) {
        ReviewAuction reviewAuction = reviewAuctionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewAuction not found with id: " + id));
        return ResponseEntity.ok(reviewAuction);
    }

    @GetMapping("/by-auction/{auctionId}")
    public ResponseEntity<List<ReviewAuction>> getAllByAuctionId(@PathVariable Long auctionId) {
        return ResponseEntity.ok(reviewAuctionRepository.findByAuctionId(auctionId));
    }

    @GetMapping("/by-seller/{sellerId}")
    public ResponseEntity<List<ReviewAuction>> getAllBySellerId(@PathVariable UUID sellerId) {
        return ResponseEntity.ok(reviewAuctionRepository.findBySellerId(sellerId));
    }

    @PostMapping
    public ResponseEntity<ReviewAuction> create(@RequestBody ReviewAuctionRequest request) {
        ReviewAuctionContext context = reviewAuctionContextRepository.findTopByOrderByIdDesc();
        if (context == null) {
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewAuctionRepository.save(reviewAuction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewAuction> update(@PathVariable Long id,
                                                @RequestBody ReviewAuctionRequest request) {
        ReviewAuction reviewAuction = reviewAuctionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewAuction not found with id: " + id));

        reviewAuction.setAuctionId(request.auctionId());
        reviewAuction.setSellerId(request.sellerId());
        reviewAuction.setApproved(request.approved());
        reviewAuction.setReason(request.reason());

        if (request.contextId() != null) {
            ReviewAuctionContext context = reviewAuctionContextRepository.findById(request.contextId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "ReviewAuctionContext not found with id: " + request.contextId()));
            reviewAuction.setReviewAuctionContext(context);
        }

        return ResponseEntity.ok(reviewAuctionRepository.save(reviewAuction));
    }

    @PatchMapping("/{id}/approval")
    public ResponseEntity<ReviewAuction> patchApproval(@PathVariable Long id,
                                                       @RequestParam Boolean approved,
                                                       @RequestParam(required = false) String reason) {
        ReviewAuction reviewAuction = reviewAuctionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewAuction not found with id: " + id));

        reviewAuction.setApproved(approved);
        if (reason != null) reviewAuction.setReason(reason);

        return ResponseEntity.ok(reviewAuctionRepository.save(reviewAuction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!reviewAuctionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ReviewAuction not found with id: " + id);
        }
        reviewAuctionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}