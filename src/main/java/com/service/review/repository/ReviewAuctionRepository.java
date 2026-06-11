package com.service.review.repository;

import com.service.review.domain.ReviewAuction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewAuctionRepository extends JpaRepository<ReviewAuction, Long> {
    List<ReviewAuction> findByAuctionId(Long auctionId);
    List<ReviewAuction> findBySellerId(UUID sellerId);
    List<ReviewAuction> findByApproved(Boolean approved);
    List<ReviewAuction> findByAuctionIdAndSellerId(Long auctionId, UUID sellerId);
}