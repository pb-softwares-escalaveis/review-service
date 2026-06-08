package com.service.review.service;

import com.service.review.domain.ReviewMessageContext;
import com.service.review.repository.ReviewMessageContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewMessageContextService {
    private final ReviewMessageContextRepository reviewMessageContextRepository;

    public ReviewMessageContext getLastVersionOfMessageReviewContext() {
        log.debug("Buscando última versão do contexto de revisão de mensagem...");

        ReviewMessageContext context = reviewMessageContextRepository.findTopByOrderByIdDesc();

        if (context == null) {
            log.warn("Nenhum ReviewMessageContext encontrado no banco de dados. " +
                    "Revisões de mensagem não poderão ser processadas até que um contexto seja cadastrado.");
        } else {
            log.debug("ReviewMessageContext carregado com sucesso. contextId={} | tamanho={} caracteres",
                    context.getId(), context.getContext().length());
        }

        return context;
    }
}