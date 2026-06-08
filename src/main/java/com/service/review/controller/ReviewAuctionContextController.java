package com.service.review.controller;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.dto.ReviewAuctionContextRequest;
import com.service.review.repository.ReviewAuctionContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/review-auction-contexts")
@RequiredArgsConstructor
public class ReviewAuctionContextController {
    private final ReviewAuctionContextRepository reviewAuctionContextRepository;

    @GetMapping
    public ResponseEntity<List<ReviewAuctionContext>> getAll() {
        log.debug("GET /api/v1/review-auction-contexts — buscando todos os contextos");
        List<ReviewAuctionContext> result = reviewAuctionContextRepository.findAll();
        log.debug("Total de contextos encontrados: {}", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewAuctionContext> getById(@PathVariable Long id) {
        log.debug("GET /api/v1/review-auction-contexts/{} — buscando contexto por ID", id);
        ReviewAuctionContext context = reviewAuctionContextRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewAuctionContext nao encontrado. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewAuctionContext not found with id: " + id);
                });
        log.debug("ReviewAuctionContext encontrado. id={}", id);
        return ResponseEntity.ok(context);
    }

    @GetMapping("/latest")
    public ResponseEntity<ReviewAuctionContext> getLatest() {
        log.debug("GET /api/v1/review-auction-contexts/latest — buscando contexto mais recente");
        ReviewAuctionContext context = reviewAuctionContextRepository.findTopByOrderByIdDesc();
        if (context == null) {
            log.warn("Nenhum ReviewAuctionContext encontrado no banco de dados.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No ReviewAuctionContext found.");
        }
        log.debug("Contexto mais recente encontrado. id={}", context.getId());
        return ResponseEntity.ok(context);
    }

    @PostMapping
    public ResponseEntity<ReviewAuctionContext> create(@RequestBody ReviewAuctionContextRequest request) {
        log.info("POST /api/v1/review-auction-contexts — criando novo contexto. tamanho={} caracteres",
                request.context().length());

        ReviewAuctionContext context = new ReviewAuctionContext();
        context.setContext(request.context());
        ReviewAuctionContext saved = reviewAuctionContextRepository.save(context);

        log.info("ReviewAuctionContext criado com sucesso. id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewAuctionContext> update(@PathVariable Long id,
                                                       @RequestBody ReviewAuctionContextRequest request) {
        log.info("PUT /api/v1/review-auction-contexts/{} — atualizando contexto", id);

        ReviewAuctionContext context = reviewAuctionContextRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewAuctionContext nao encontrado para atualizacao. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewAuctionContext not found with id: " + id);
                });

        context.setContext(request.context());
        ReviewAuctionContext saved = reviewAuctionContextRepository.save(context);

        log.info("ReviewAuctionContext atualizado com sucesso. id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReviewAuctionContext> patch(@PathVariable Long id,
                                                      @RequestParam String context) {
        log.info("PATCH /api/v1/review-auction-contexts/{} — atualizando texto do contexto. tamanho={} caracteres",
                id, context.length());

        ReviewAuctionContext existing = reviewAuctionContextRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewAuctionContext nao encontrado para patch. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewAuctionContext not found with id: " + id);
                });

        existing.setContext(context);
        ReviewAuctionContext saved = reviewAuctionContextRepository.save(existing);

        log.info("Texto do contexto atualizado com sucesso. id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/review-auction-contexts/{} — removendo contexto", id);

        if (!reviewAuctionContextRepository.existsById(id)) {
            log.warn("ReviewAuctionContext nao encontrado para exclusao. id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ReviewAuctionContext not found with id: " + id);
        }

        reviewAuctionContextRepository.deleteById(id);
        log.info("ReviewAuctionContext removido com sucesso. id={}", id);

        return ResponseEntity.noContent().build();
    }
}