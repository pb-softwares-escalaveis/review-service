package com.service.review.controller;

import com.service.review.domain.ReviewMessageContext;
import com.service.review.dto.ReviewMessageContextRequest;
import com.service.review.repository.ReviewMessageContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/review-message-contexts")
@RequiredArgsConstructor
public class ReviewMessageContextController {
    private final ReviewMessageContextRepository reviewMessageContextRepository;

    @GetMapping
    public ResponseEntity<List<ReviewMessageContext>> getAll() {
        log.debug("GET /api/v1/review-message-contexts — buscando todos os contextos");
        List<ReviewMessageContext> result = reviewMessageContextRepository.findAll();
        log.debug("Total de contextos de mensagem encontrados: {}", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewMessageContext> getById(@PathVariable Long id) {
        log.debug("GET /api/v1/review-message-contexts/{} — buscando contexto por ID", id);
        ReviewMessageContext context = reviewMessageContextRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewMessageContext nao encontrado. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewMessageContext not found with id: " + id);
                });
        log.debug("ReviewMessageContext encontrado. id={}", id);
        return ResponseEntity.ok(context);
    }

    @GetMapping("/latest")
    public ResponseEntity<ReviewMessageContext> getLatest() {
        log.debug("GET /api/v1/review-message-contexts/latest — buscando contexto mais recente");
        ReviewMessageContext context = reviewMessageContextRepository.findTopByOrderByIdDesc();
        if (context == null) {
            log.warn("Nenhum ReviewMessageContext encontrado no banco de dados.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No ReviewMessageContext found.");
        }
        log.debug("Contexto mais recente encontrado. id={}", context.getId());
        return ResponseEntity.ok(context);
    }

    @PostMapping
    public ResponseEntity<ReviewMessageContext> create(@RequestBody ReviewMessageContextRequest request) {
        log.info("POST /api/v1/review-message-contexts — criando novo contexto. tamanho={} caracteres",
                request.context().length());

        ReviewMessageContext context = new ReviewMessageContext();
        context.setContext(request.context());
        ReviewMessageContext saved = reviewMessageContextRepository.save(context);

        log.info("ReviewMessageContext criado com sucesso. id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewMessageContext> update(@PathVariable Long id,
                                                       @RequestBody ReviewMessageContextRequest request) {
        log.info("PUT /api/v1/review-message-contexts/{} — atualizando contexto", id);

        ReviewMessageContext context = reviewMessageContextRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewMessageContext nao encontrado para atualizacao. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewMessageContext not found with id: " + id);
                });

        context.setContext(request.context());
        ReviewMessageContext saved = reviewMessageContextRepository.save(context);

        log.info("ReviewMessageContext atualizado com sucesso. id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReviewMessageContext> patch(@PathVariable Long id,
                                                      @RequestParam String context) {
        log.info("PATCH /api/v1/review-message-contexts/{} — atualizando texto do contexto. tamanho={} caracteres",
                id, context.length());

        ReviewMessageContext existing = reviewMessageContextRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewMessageContext nao encontrado para patch. id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ReviewMessageContext not found with id: " + id);
                });

        existing.setContext(context);
        ReviewMessageContext saved = reviewMessageContextRepository.save(existing);

        log.info("Texto do contexto atualizado com sucesso. id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/review-message-contexts/{} — removendo contexto", id);

        if (!reviewMessageContextRepository.existsById(id)) {
            log.warn("ReviewMessageContext nao encontrado para exclusao. id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ReviewMessageContext not found with id: " + id);
        }

        reviewMessageContextRepository.deleteById(id);
        log.info("ReviewMessageContext removido com sucesso. id={}", id);

        return ResponseEntity.noContent().build();
    }
}