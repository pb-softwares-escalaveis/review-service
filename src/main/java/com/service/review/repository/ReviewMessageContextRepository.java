package com.service.review.repository;

import com.service.review.domain.ReviewAuctionContext;
import com.service.review.domain.ReviewMessageContext;
import com.service.review.enums.ContextType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewMessageContextRepository extends JpaRepository<ReviewMessageContext, Long> {
    ReviewMessageContext findTopByTypeOrderByIdDesc(ContextType type);
}
