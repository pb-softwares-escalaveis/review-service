package com.service.review.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.service.review.domain.ReviewContext;
import com.service.review.dto.ReviewResponse;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewService() {
        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            try {
                Dotenv dotenv = Dotenv.load();
                apiKey = dotenv.get("GEMINI_API_KEY");
            } catch (RuntimeException ignored) {
                apiKey = null;
            }
        }

        if (apiKey == null || apiKey.isBlank()) {
            this.client = null;
            return;
        }

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @Retry(name = "reviewService", fallbackMethod = "fallback")
    public ReviewResponse analyze(String input, ReviewContext context) {
        if (client == null) {
            throw new IllegalStateException("GEMINI_API_KEY nao configurada");
        }

        String model = "gemini-2.5-flash";

        Content content = Content.builder()
                .role("user")
                .parts(List.of(
                        Part.fromText(input)
                ))
                .build();

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .systemInstruction(
                                Content.fromParts(
                                        Part.fromText(context.getContext())
                                )
                        )
                        .build();

        GenerateContentResponse response =
                client.models.generateContent(model, List.of(content), config);

        try {
            return objectMapper.readValue(
                    response.text(),
                    ReviewResponse.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter resposta da IA", e);
        }
    }

    public ReviewResponse fallback(Throwable t) {
        throw new RuntimeException("Falhou após retries", t);
    }
}
