package com.service.review.service;

import com.service.review.domain.Auction;
import com.service.review.domain.ReviewContext;
import com.service.review.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final ReviewService ReviewService;

    public ReviewResponse reviewAuction(Auction auction) {
        return ReviewService.analyze(auction.toString(), auction.getReviewContext());
    }
}
