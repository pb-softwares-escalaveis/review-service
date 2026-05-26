package com.service.review.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {
    private Long userId;
    private String message;
    private ReviewContext reviewContext;

    public Message(Long userId, String message) {
        this.userId = userId;
        this.message = message;
        this.reviewContext = ReviewContext.MESSAGE;
    }

    @Override
    public String toString() {
        return "Message{" +
                "userId=" + userId +
                ", message='" + message +
                '}';
    }
}
