package com.service.review.dto;

public record ReviewResponse(
        boolean approved,
        String reprovedReason
) { }
