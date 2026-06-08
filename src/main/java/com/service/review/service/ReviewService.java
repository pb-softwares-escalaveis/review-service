package com.service.review.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.service.review.dto.ReviewResponse;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private static final String MODEL = "gemini-2.5-flash";
    private final Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewService() {
        log.info("Inicializando ReviewService — carregando GEMINI_API_KEY...");

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY não encontrada nas variáveis de ambiente. Tentando carregar via .env...");
            try {
                Dotenv dotenv = Dotenv.load();
                apiKey = dotenv.get("GEMINI_API_KEY");
                log.info("GEMINI_API_KEY carregada com sucesso via arquivo .env.");
            } catch (RuntimeException e) {
                log.warn("Falha ao carregar arquivo .env: {}. ReviewService será inicializado sem cliente Gemini.", e.getMessage());
                apiKey = null;
            }
        } else {
            log.info("GEMINI_API_KEY carregada com sucesso via variável de ambiente.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            log.error("GEMINI_API_KEY ausente. O ReviewService não poderá realizar análises — nenhum cliente Gemini será criado.");
            this.client = null;
            return;
        }

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
        log.info("Cliente Gemini inicializado com sucesso. Modelo padrão: {}", MODEL);
    }

    @Retry(name = "reviewService", fallbackMethod = "fallback")
    public ReviewResponse analyze(String input, String context) {
        return analyze(input, context, null, null);
    }

    @Retry(name = "reviewService", fallbackMethod = "fallback")
    public ReviewResponse analyze(String input, String context, byte[] imageBytes, String mimeType) {
        log.debug("Iniciando análise via Gemini. Tamanho do input: {} caracteres | Tamanho do contexto: {} caracteres",
                input.length(), context.length());

        if (client == null) {
            log.error("Tentativa de análise sem cliente Gemini configurado. GEMINI_API_KEY pode estar ausente.");
            throw new IllegalStateException("GEMINI_API_KEY nao configurada");
        }

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(input));

        if (imageBytes != null && mimeType != null) {
            parts.add(Part.builder()
                    .inlineData(Blob.builder()
                            .data(imageBytes)
                            .mimeType(mimeType)
                            .build())
                    .build());
        }

        Content content = Content.builder()
                .role("user")
                .parts(parts)
                .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .systemInstruction(Content.fromParts(Part.fromText(context)))
                .build();

        log.debug("Enviando requisição ao modelo Gemini '{}'...", MODEL);

        GenerateContentResponse response = client.models.generateContent(MODEL, List.of(content), config);

        log.debug("Resposta recebida do Gemini. Tamanho da resposta: {} caracteres", response.text().length());

        try {
            ReviewResponse reviewResponse = objectMapper.readValue(response.text(), ReviewResponse.class);
            log.info("Análise concluída. Resultado: approved={} | reason='{}'",
                    reviewResponse.approved(), reviewResponse.reason());
            return reviewResponse;
        } catch (Exception e) {
            log.error("Falha ao desserializar resposta do Gemini. Resposta recebida: '{}'. Erro: {}",
                    response.text(), e.getMessage(), e);
            throw new RuntimeException("Erro ao converter resposta da IA", e);
        }
    }

    public ReviewResponse fallback(Throwable t) {
        log.error("Todos os retries esgotados no ReviewService. Causa raiz: {}", t.getMessage(), t);
        throw new RuntimeException("Falhou após retries", t);
    }
}