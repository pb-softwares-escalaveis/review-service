package com.service.review.service;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.repository.ReviewAuctionContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewAuctionContextService {
    ReviewAuctionContextRepository reviewAuctionContextRepository;

    public ReviewAuctionContext getLastVersionOfAuctionReviewContext() {
        return reviewAuctionContextRepository.findTopByOrderByIdDesc();
    }
}
