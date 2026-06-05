package com.service.review.service;

import com.service.review.domain.ReviewMessageContext;
import com.service.review.repository.ReviewMessageContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewMessageContextService {
    ReviewMessageContextRepository reviewMessageContextRepository;

    public ReviewMessageContext getLastVersionOfMessageReviewContext() {
        return reviewMessageContextRepository.findTopByOrderByIdDesc();
    }
}
