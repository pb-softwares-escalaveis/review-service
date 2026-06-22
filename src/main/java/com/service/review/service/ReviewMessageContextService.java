package com.service.review.service;

import com.service.review.domain.ReviewMessageContext;
import com.service.review.enums.ContextType;
import com.service.review.metrics.ReviewMetrics;
import com.service.review.repository.ReviewMessageContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewMessageContextService {
    private final ReviewMessageContextRepository reviewMessageContextRepository;
    private final ReviewMetrics reviewMetrics;

    public ReviewMessageContext getContext(ContextType type) {
        log.debug("Buscando última versão do contexto de revisão de mensagem. type={}", type);

        ReviewMessageContext result = reviewMessageContextRepository.findTopByTypeOrderByIdDesc(type);

        if (result == null) {
            reviewMetrics.incrementContextFetchFailure("message", type);
            log.warn("Nenhum ReviewMessageContext encontrado para type={}. " +
                    "Revisões de mensagem não poderão ser processadas até que um contexto seja cadastrado.", type);
        } else {
            reviewMetrics.incrementContextFetched("message", type);
            log.debug("ReviewMessageContext carregado com sucesso. contextId={} | type={} | tamanho={} caracteres",
                    result.getId(), type, result.getContext().length());
        }

        return result;
    }
}