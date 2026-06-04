package com.service.review.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false, unique = true, name = "id_anuncio")
    private Long auctionId;

    @Column(nullable = false, name = "titulo")
    private String title;

    @Column(nullable = false, name = "descricao")
    private String description;

    @Column(nullable = false, name = "imagem")
    private String image;

    private ReviewContext reviewContext;

    public Auction(Long auctionId, String title, String description, String image) {
        this.auctionId = auctionId;
        this.title = title;
        this.description = description;
        this.image = image;
        this.reviewContext = ReviewContext.AUCTION;
    }

    public Auction(String title, String description, String image, ReviewContext reviewContext) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.reviewContext = reviewContext != null ? reviewContext : ReviewContext.AUCTION;
    }

    @Override
    public String toString() {
        return "Auction{" +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image +
                '}';
    }
}
