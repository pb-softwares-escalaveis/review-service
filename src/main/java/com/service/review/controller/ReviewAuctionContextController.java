package com.service.review.controller;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.dto.ReviewAuctionContextRequest;
import com.service.review.repository.ReviewAuctionContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review-auction-contexts")
@RequiredArgsConstructor
public class ReviewAuctionContextController {
    private final ReviewAuctionContextRepository reviewAuctionContextRepository;

    @GetMapping
    public ResponseEntity<List<ReviewAuctionContext>> getAll() {
        return ResponseEntity.ok(reviewAuctionContextRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewAuctionContext> getById(@PathVariable Long id) {
        ReviewAuctionContext context = reviewAuctionContextRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewAuctionContext not found with id: " + id));
        return ResponseEntity.ok(context);
    }

    @GetMapping("/latest")
    public ResponseEntity<ReviewAuctionContext> getLatest() {
        ReviewAuctionContext context = reviewAuctionContextRepository.findTopByOrderByIdDesc();
        if (context == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No ReviewAuctionContext found.");
        }
        return ResponseEntity.ok(context);
    }

    @PostMapping
    public ResponseEntity<ReviewAuctionContext> create(@RequestBody ReviewAuctionContextRequest request) {
        ReviewAuctionContext context = new ReviewAuctionContext();
        context.setContext(request.context());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewAuctionContextRepository.save(context));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewAuctionContext> update(@PathVariable Long id,
                                                       @RequestBody ReviewAuctionContextRequest request) {
        ReviewAuctionContext context = reviewAuctionContextRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewAuctionContext not found with id: " + id));

        context.setContext(request.context());
        return ResponseEntity.ok(reviewAuctionContextRepository.save(context));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReviewAuctionContext> patch(@PathVariable Long id,
                                                      @RequestParam String context) {
        ReviewAuctionContext existing = reviewAuctionContextRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewAuctionContext not found with id: " + id));

        existing.setContext(context);
        return ResponseEntity.ok(reviewAuctionContextRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!reviewAuctionContextRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ReviewAuctionContext not found with id: " + id);
        }
        reviewAuctionContextRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}