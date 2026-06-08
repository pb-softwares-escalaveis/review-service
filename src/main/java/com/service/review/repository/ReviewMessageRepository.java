package com.service.review.repository;

import com.service.review.domain.ReviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewMessageRepository extends JpaRepository<ReviewMessage, Long> {
    List<ReviewMessage> findByAuctionId(Long auctionId);
    List<ReviewMessage> findBySellerId(UUID sellerId);
    List<ReviewMessage> findByMessageId(Long messageId);
    List<ReviewMessage> findByApproved(Boolean approved);
    List<ReviewMessage> findByAuctionIdAndSellerId(Long auctionId, UUID sellerId);
    List<ReviewMessage> findByAuctionIdAndMessageId(Long auctionId, Long messageId);
}