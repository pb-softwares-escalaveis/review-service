package com.service.review.service;

import com.service.review.dto.ReviewResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("dev")
public class MockReviewService implements ReviewService {
    @Override
    public ReviewResponse analyze(String input, String context) {
        return analyze(input, context, null, null);
    }

    @Override
    public ReviewResponse analyze(
            String input,
            String context,
            byte[] imageBytes,
            String mimeType) {

        String normalized = input.toLowerCase();

        boolean approved =
                !normalized.contains("golpe")
                        && !normalized.contains("fraude")
                        && !normalized.contains("arma")
                        && !normalized.contains("drogas");

        String reason = approved
                ? "Aprovado"
                : "Reprovado pelo mock de desenvolvimento.";

        log.info("MockReviewService -> approved={}", approved);

        return new ReviewResponse(
                approved,
                reason
        );
    }
}