package com.service.review.service;

import com.service.review.dto.ReviewResponse;

public interface ReviewService {
    ReviewResponse analyze(String input, String context);
    ReviewResponse analyze(String input,
                           String context,
                           byte[] imageBytes,
                           String mimeType);
}