package com.service.review.controller;

import com.service.review.domain.Message;
import com.service.review.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping("/message")
    public String reviewMessage(@RequestBody Message message) {
        return messageService.reviewMessage(message);
    }
}
