package com.service.review.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final Client client;

    public ReviewService() {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("GEMINI_API_KEY");

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String analyze(String input) {
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
                                        Part.fromText("""
                                                Você é um filtro de conteúdo proibido. Sua ÚNICA função é detectar conteúdo explicitamente proibido.
                                                
                                                REPROVE apenas se houver:
                                                - palavrões, xingamentos ou ofensas
                                                - conteúdo sexual ou nudez
                                                - violência ou drogas
                                                - contato externo (telefone, WhatsApp, email, Instagram, links)
                                                - golpes ou conteúdo ilegal
                                                
                                                IGNORE completamente:
                                                - se a marca/modelo na imagem bate com o texto
                                                - coerência entre título, descrição e imagem
                                                - qualidade ou veracidade do anúncio
                                                - qualquer outro critério fora da lista acima
                                                
                                                Se não houver nenhum item da lista de reprovação, SEMPRE aprove.
                                                
                                                Retorne apenas JSON com:
                                                approved (boolean)
                                                reason (string) *only if approved is false*
                                                """)
                                )
                        )
                        .build();

        GenerateContentResponse response =
                client.models.generateContent(model, List.of(content), config);

        return response.text();
    }
}