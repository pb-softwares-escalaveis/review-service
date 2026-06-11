package com.service.review.service;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.enums.ContextType;
import com.service.review.repository.ReviewAuctionContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAuctionContextService {
    private final ReviewAuctionContextRepository reviewAuctionContextRepository;

    public ReviewAuctionContext getContext(ContextType type) {
        log.debug("Buscando última versão do contexto de revisão de leilão. type={}", type);

        ReviewAuctionContext context = reviewAuctionContextRepository.findTopByTypeOrderByIdDesc(type);

        if (context == null) {
            log.warn("Nenhum ReviewAuctionContext encontrado para type={}. " +
                    "Revisões não poderão ser processadas até que um contexto seja cadastrado.", type);
        } else {
            log.debug("ReviewAuctionContext carregado com sucesso. contextId={} | type={} | tamanho={} caracteres",
                    context.getId(), type, context.getContext().length());
        }

        return context;
    }
}