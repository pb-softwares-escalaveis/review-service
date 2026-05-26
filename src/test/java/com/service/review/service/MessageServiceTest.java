package com.service.review.service;

import com.service.review.domain.Message;
import com.service.review.dto.ReviewResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {
    private static MessageService messageService;

    @BeforeAll
    static void setup() {
        messageService = new MessageService(new ReviewService());
    }

    @Test
    public void ReviewedMessageShouldBeTrue() {
        Message message = new Message(1L, "Olá, como você esta?");
        ReviewResponse reviewResponse = messageService.reviewMessage(message);
        System.out.println(reviewResponse.toString());
        assertTrue(reviewResponse.approved());
    }

    @Test
    public void ReviewedMessageShouldBeFalse(){
        Message message2 = new Message(2L, "Tem interesse em uma pistola 45mm com numeracao raspada?");
        ReviewResponse reviewResponse = messageService.reviewMessage(message2);
        System.out.println(reviewResponse.toString());
        assertFalse(reviewResponse.approved());
    }

}