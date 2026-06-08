package com.service.review.service;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.repository.ReviewAuctionContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAuctionContextService {
    private final ReviewAuctionContextRepository reviewAuctionContextRepository;

    public ReviewAuctionContext getLastVersionOfAuctionReviewContext() {
        log.debug("Buscando última versão do contexto de revisão de leilão...");

        ReviewAuctionContext context = reviewAuctionContextRepository.findTopByOrderByIdDesc();

        if (context == null) {
            log.warn("Nenhum ReviewAuctionContext encontrado no banco de dados. " +
                    "Revisões de leilão não poderão ser processadas até que um contexto seja cadastrado.");
        } else {
            log.debug("ReviewAuctionContext carregado com sucesso. contextId={} | tamanho={} caracteres",
                    context.getId(), context.getContext().length());
        }

        return context;
    }
}