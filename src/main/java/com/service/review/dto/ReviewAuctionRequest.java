package com.service.review.dto;

import java.util.UUID;

public record ReviewAuctionRequest(
        Long auctionId,
        UUID sellerId,
        Boolean approved,
        String reason,
        Long contextId
) {
}
