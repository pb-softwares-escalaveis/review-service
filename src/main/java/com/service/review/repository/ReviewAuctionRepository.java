package com.service.review.repository;

import com.service.review.domain.ReviewAuction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAuctionRepository extends JpaRepository<ReviewAuction, Long> {
}
