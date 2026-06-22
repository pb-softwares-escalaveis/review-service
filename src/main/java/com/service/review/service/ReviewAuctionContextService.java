package com.service.review.service;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.enums.ContextType;
import com.service.review.metrics.ReviewMetrics;
import com.service.review.repository.ReviewAuctionContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAuctionContextService {
    private final ReviewAuctionContextRepository reviewAuctionContextRepository;
    private final ReviewMetrics reviewMetrics;

    public ReviewAuctionContext getContext(ContextType type) {
        log.debug("Buscando última versão do contexto de revisão de leilão. type={}", type);

        ReviewAuctionContext result = reviewAuctionContextRepository.findTopByTypeOrderByIdDesc(type);

        if (result == null) {
            reviewMetrics.incrementContextFetchFailure("auction", type);
            log.warn("Nenhum ReviewAuctionContext encontrado para type={}. " +
                    "Revisões não poderão ser processadas até que um contexto seja cadastrado.", type);
        } else {
            reviewMetrics.incrementContextFetched("auction", type);
            log.debug("ReviewAuctionContext carregado com sucesso. contextId={} | type={} | tamanho={} caracteres",
                    result.getId(), type, result.getContext().length());
        }

        return result;
    }
}