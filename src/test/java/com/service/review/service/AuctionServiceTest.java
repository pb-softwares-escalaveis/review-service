package com.service.review.service;

import com.service.review.domain.Auction;
import com.service.review.dto.ReviewResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceTest {
    private static AuctionService auctionService;

    @BeforeAll
    static void setup() {
        auctionService = new AuctionService(new ReviewService());
    }

    @Test
    void shouldCreateAuctionWithBucketImage() {
        String imageKey = "bucket/" + UUID.randomUUID() + ".jpg";

        Auction auction = new Auction(
                1L,
                "PS5",
                "Console novo",
                "Games",
                imageKey
        );

        ReviewResponse result = auctionService.reviewAuction(auction);

        System.out.println(result);
        assertNotNull(result);
        assertTrue(auction.getImage().startsWith("bucket/"));
    }
}