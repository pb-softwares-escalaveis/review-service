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
}
