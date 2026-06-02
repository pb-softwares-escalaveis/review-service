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
    private String title;
    private String description;
    private String category;
    private String image;
    private ReviewContext reviewContext;

    public Auction(String title, String description, String category, String image) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.image = image;
        this.reviewContext = ReviewContext.AUCTION;
    }

    @Override
    public String toString() {
        return "Auction{" +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", image='" + image +
                '}';
    }
}
