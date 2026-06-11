package com.service.review.dto;

import com.service.review.enums.ContextType;

public record ReviewMessageContextRequest(
        String context,
        ContextType type
) {
}