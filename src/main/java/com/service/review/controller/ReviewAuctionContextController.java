package com.service.review.controller;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.dto.ReviewAuctionContextRequest;
import com.service.review.enums.ContextType;
import com.service.review.exception.ResourceNotFoundException;
import com.service.review.repository.ReviewAuctionContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                    return new ResourceNotFoundException("ReviewAuctionContext not found with id: " + id);
                });
        log.debug("ReviewAuctionContext encontrado. id={}", id);
        return ResponseEntity.ok(context);
    }

    @GetMapping("/latest")
    public ResponseEntity<ReviewAuctionContext> getLatest(@RequestParam ContextType type) {
        log.debug("GET /api/v1/review-auction-contexts/latest — buscando contexto mais recente. type={}", type);
        ReviewAuctionContext context = reviewAuctionContextRepository.findTopByTypeOrderByIdDesc(type);
        if (context == null) {
            log.warn("Nenhum ReviewAuctionContext encontrado para type={}", type);
            throw new ResourceNotFoundException("No ReviewAuctionContext found for type: " + type);
        }
        log.debug("Contexto mais recente encontrado. id={} | type={}", context.getId(), type);
        return ResponseEntity.ok(context);
    }

    @PostMapping
    public ResponseEntity<ReviewAuctionContext> create(@RequestBody ReviewAuctionContextRequest request) {
        log.info("POST /api/v1/review-auction-contexts — criando novo contexto. type={} | tamanho={} caracteres",
                request.type(), request.context().length());

        ReviewAuctionContext context = new ReviewAuctionContext();
        context.setContext(request.context());
        context.setType(request.type());
        ReviewAuctionContext saved = reviewAuctionContextRepository.save(context);

        log.info("ReviewAuctionContext criado com sucesso. id={} | type={}", saved.getId(), saved.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewAuctionContext> update(@PathVariable Long id,
                                                       @RequestBody ReviewAuctionContextRequest request) {
        log.info("PUT /api/v1/review-auction-contexts/{} — atualizando contexto. type={}", id, request.type());

        ReviewAuctionContext context = reviewAuctionContextRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ReviewAuctionContext nao encontrado para atualizacao. id={}", id);
                    return new ResourceNotFoundException("ReviewAuctionContext not found with id: " + id);
                });

        context.setContext(request.context());
        context.setType(request.type());
        ReviewAuctionContext saved = reviewAuctionContextRepository.save(context);

        log.info("ReviewAuctionContext atualizado com sucesso. id={} | type={}", saved.getId(), saved.getType());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/review-auction-contexts/{} — removendo contexto", id);

        if (!reviewAuctionContextRepository.existsById(id)) {
            log.warn("ReviewAuctionContext nao encontrado para exclusao. id={}", id);
            throw new ResourceNotFoundException("ReviewAuctionContext not found with id: " + id);
        }

        reviewAuctionContextRepository.deleteById(id);
        log.info("ReviewAuctionContext removido com sucesso. id={}", id);

        return ResponseEntity.noContent().build();
    }
}
