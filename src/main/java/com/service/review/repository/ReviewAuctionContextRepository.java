package com.service.review.repository;

import com.service.review.domain.ReviewAuctionContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAuctionContextRepository extends JpaRepository<ReviewAuctionContext, Long> {
    ReviewAuctionContext findTopByOrderByIdDesc();
}
