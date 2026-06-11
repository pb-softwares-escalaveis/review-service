package com.service.review.dto;

import com.service.review.enums.ContextType;

public record ReviewAuctionContextRequest(
        String context,
        ContextType type
) {
}