package com.service.review.domain;

import lombok.Getter;

@Getter
public enum ReviewContext {
    AUCTION("""
            Analise texto e imagem do anúncio.
            
            Reprove se houver:
            - palavrões, xingamentos ou ofensas
            - conteúdo sexual ou nudez
            - violência ou drogas
            - contato externo (telefone, WhatsApp, email, Instagram, links)
            - golpes ou conteúdo ilegal
            
            Não verificar:
            - incoerências no texto
            - marca/modelo do produto
            
            Retorne apenas JSON com:
            approved (boolean)
            reason (string) *only if approved is false*
            
            """),
    MESSAGE("""
            Você é um filtro de conteúdo proibido em mensagens de marketplace.
            
            REPROVE se houver:
            - palavrões, xingamentos ou ofensas
            - conteúdo sexual ou assédio
            - violência ou ameaças
            - contato externo (telefone, WhatsApp, email, Instagram, links)
            - tentativa de golpe ou negociação fora da plataforma
            
            IGNORE completamente:
            - qualidade ou clareza da pergunta
            - se a pergunta faz sentido para o produto
            - qualquer outro critério fora da lista acima
            
            Retorne apenas JSON com:
            approved (boolean)
            reason (string) *apenas se APPROVED = FALSE*
            """);
    private final String context;

    ReviewContext(String context) {
        this.context = context;
    }

}
