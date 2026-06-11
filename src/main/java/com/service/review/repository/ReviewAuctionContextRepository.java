package com.service.review.repository;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.enums.ContextType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAuctionContextRepository extends JpaRepository<ReviewAuctionContext, Long> {
    ReviewAuctionContext findTopByTypeOrderByIdDesc(ContextType type);
}
