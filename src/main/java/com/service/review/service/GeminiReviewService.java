package com.service.review.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.service.review.dto.ReviewResponse;
import com.service.review.exception.ReviewProviderException;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Profile("prod")
@Service
public class GeminiReviewService implements ReviewService {
    private static final String MODEL = "gemini-2.5-flash";
    private final Client client;
    private final ObjectMapper objectMapper;

    public GeminiReviewService(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");

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

    @Retry(name = "geminiReviewService")
    @CircuitBreaker(name = "geminiReviewService", fallbackMethod = "fallback_message")
    public ReviewResponse analyze(String input, String context) {
        return analyze(input, context, null, null);
    }

    @Retry(name = "geminiReviewService")
    @CircuitBreaker(name = "geminiReviewService", fallbackMethod = "fallback_auction")
    public ReviewResponse analyze(String input, String context, byte[] imageBytes, String mimeType) {
        validateRequest(input, context);

        log.debug("Iniciando análise via Gemini. Tamanho do input: {} caracteres | Tamanho do contexto: {} caracteres",
                input.length(), context.length());

        if (client == null) {
            log.error("Tentativa de análise sem cliente Gemini configurado. GEMINI_API_KEY pode estar ausente.");
            throw new ReviewProviderException("GEMINI_API_KEY nao configurada");
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

        GenerateContentResponse response = generateContent(content, config);
        String responseText = extractResponseText(response);

        log.debug("Resposta recebida do Gemini. Tamanho da resposta: {} caracteres", responseText.length());

        try {
            ReviewResponse reviewResponse = objectMapper.readValue(responseText, ReviewResponse.class);
            log.info("Análise concluída. Resultado: approved={} | reason='{}'",
                    reviewResponse.approved(), reviewResponse.repprovedReason());
            return reviewResponse;
        } catch (Exception e) {
            log.error("Falha ao desserializar resposta do Gemini. Resposta recebida: '{}'. Erro: {}",
                    responseText, e.getMessage(), e);
            throw new ReviewProviderException("Erro ao converter resposta da IA", e);
        }
    }

    private void validateRequest(String input, String context) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input para analise nao pode ser vazio");
        }

        if (context == null || context.isBlank()) {
            throw new IllegalArgumentException("Contexto para analise nao pode ser vazio");
        }
    }

    private GenerateContentResponse generateContent(Content content, GenerateContentConfig config) {
        try {
            return client.models.generateContent(MODEL, List.of(content), config);
        } catch (Exception e) {
            log.error("Falha na chamada ao Gemini. model={} | erro={}", MODEL, e.getMessage(), e);
            throw new ReviewProviderException("Erro ao chamar Gemini", e);
        }
    }

    private String extractResponseText(GenerateContentResponse response) {
        if (response == null) {
            throw new ReviewProviderException("Resposta nula do Gemini");
        }

        String responseText;
        try {
            responseText = response.text();
        } catch (Exception e) {
            throw new ReviewProviderException("Erro ao ler resposta do Gemini", e);
        }

        if (responseText == null || responseText.isBlank()) {
            throw new ReviewProviderException("Resposta vazia do Gemini");
        }

        return responseText;
    }

    public ReviewResponse fallback_message(String input, String context, Throwable t) {
        return fallback(t);
    }

    public ReviewResponse fallback_auction(String input, String context, byte[] imageBytes, String mimeType, Throwable t) {
        return fallback(t);
    }

    private ReviewResponse fallback(Throwable t) {
        if (t instanceof CallNotPermittedException) {
            log.error("Circuit breaker aberto no GeminiReviewService. Chamada bloqueada antes de acionar o Gemini. Erro: {}",
                    t.getMessage(), t);
        } else {
            log.error("Fallback acionado no GeminiReviewService. Retries esgotados ou chamada ao Gemini falhou. Causa raiz: {}",
                    t.getMessage(), t);
        }

        throw new ReviewProviderException("Falhou apos retries ou circuito aberto", t);
    }
}
