package com.service.review.dto;

import java.util.UUID;

public record ReviewMessageRequest(
        Long auctionId,
        UUID sellerId,
        Long messageId,
        Boolean approved,
        String reason,
        Long contextId
) {}
