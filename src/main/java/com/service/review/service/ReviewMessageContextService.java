package com.service.review.service;

import com.service.review.domain.ReviewMessageContext;
import com.service.review.enums.ContextType;
import com.service.review.repository.ReviewMessageContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewMessageContextService {
    private final ReviewMessageContextRepository reviewMessageContextRepository;

    public ReviewMessageContext getContext(ContextType type) {
        log.debug("Buscando última versão do contexto de revisão de mensagem. type={}", type);

        ReviewMessageContext context = reviewMessageContextRepository.findTopByTypeOrderByIdDesc(type);

        if (context == null) {
            log.warn("Nenhum ReviewMessageContext encontrado para type={}. " +
                    "Revisões de mensagem não poderão ser processadas até que um contexto seja cadastrado.", type);
        } else {
            log.debug("ReviewMessageContext carregado com sucesso. contextId={} | type={} | tamanho={} caracteres",
                    context.getId(), type, context.getContext().length());
        }

        return context;
    }
}