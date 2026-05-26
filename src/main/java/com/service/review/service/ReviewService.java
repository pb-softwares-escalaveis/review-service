package com.service.review.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.service.review.domain.ReviewContext;
import com.service.review.dto.ReviewResponse;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewService() {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("GEMINI_API_KEY");

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public ReviewResponse analyze(String input, ReviewContext context) {
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


}