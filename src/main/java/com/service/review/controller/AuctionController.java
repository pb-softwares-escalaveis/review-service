package com.service.review.controller;

import com.service.review.domain.Auction;
import com.service.review.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    @PostMapping("/auction")
    public String reviewAuction(@RequestBody Auction auction) {
        return auctionService.reviewAuction(auction);
    }
}
