package com.service.review.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Auction {
    private Long userId;
    private String title;
    private String description;
    private String category;
    private String image;
    private ReviewContext reviewContext;

    public Auction(Long userId, String title, String description, String category, String image) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.image = image;
        this.reviewContext = ReviewContext.AUCTION;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", image='" + image +
                '}';
    }
}
