package com.service.review.service;

import com.service.review.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final ReviewService ReviewService;

    public String reviewMessage(Message message) {
        return ReviewService.analyze(message.toString());
    }
}
