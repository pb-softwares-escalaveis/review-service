package com.service.review.service;

import com.service.review.domain.Auction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final ReviewService ReviewService;

    public String reviewAuction(Auction auction) {
        return ReviewService.analyze(auction.toString());
    }
}
