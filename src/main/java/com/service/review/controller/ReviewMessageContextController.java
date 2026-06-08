package com.service.review.controller;

import com.service.review.domain.ReviewMessageContext;
import com.service.review.dto.ReviewMessageContextRequest;
import com.service.review.repository.ReviewMessageContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review-message-contexts")
@RequiredArgsConstructor
public class ReviewMessageContextController {
    private final ReviewMessageContextRepository reviewMessageContextRepository;

    @GetMapping
    public ResponseEntity<List<ReviewMessageContext>> getAll() {
        return ResponseEntity.ok(reviewMessageContextRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewMessageContext> getById(@PathVariable Long id) {
        ReviewMessageContext context = reviewMessageContextRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewMessageContext not found with id: " + id));
        return ResponseEntity.ok(context);
    }

    @GetMapping("/latest")
    public ResponseEntity<ReviewMessageContext> getLatest() {
        ReviewMessageContext context = reviewMessageContextRepository.findTopByOrderByIdDesc();
        if (context == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No ReviewMessageContext found.");
        }
        return ResponseEntity.ok(context);
    }

    @PostMapping
    public ResponseEntity<ReviewMessageContext> create(@RequestBody ReviewMessageContextRequest request) {
        ReviewMessageContext context = new ReviewMessageContext();
        context.setContext(request.context());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewMessageContextRepository.save(context));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewMessageContext> update(@PathVariable Long id,
                                                       @RequestBody ReviewMessageContextRequest request) {
        ReviewMessageContext context = reviewMessageContextRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewMessageContext not found with id: " + id));

        context.setContext(request.context());
        return ResponseEntity.ok(reviewMessageContextRepository.save(context));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReviewMessageContext> patch(@PathVariable Long id,
                                                      @RequestParam String context) {
        ReviewMessageContext existing = reviewMessageContextRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ReviewMessageContext not found with id: " + id));

        existing.setContext(context);
        return ResponseEntity.ok(reviewMessageContextRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!reviewMessageContextRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ReviewMessageContext not found with id: " + id);
        }
        reviewMessageContextRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}