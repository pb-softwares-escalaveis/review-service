package com.service.review.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "message")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false, unique = true, name = "id_usuario")
    private Long userId;

    @Column(nullable = false)
    private String message;

    private ReviewContext reviewContext;

    public Message(Long userId, String message) {
        this.userId = userId;
        this.message = message;
        this.reviewContext = ReviewContext.MESSAGE;
    }

    public Message(Long userId, String message, ReviewContext reviewContext) {
        this.userId = userId;
        this.message = message;
        this.reviewContext = reviewContext != null ? reviewContext : ReviewContext.MESSAGE;
    }

    @Override
    public String toString() {
        return "Message{" +
                "userId=" + userId +
                ", message='" + message +
                '}';
    }
}
