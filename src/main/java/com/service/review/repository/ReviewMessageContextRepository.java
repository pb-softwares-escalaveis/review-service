package com.service.review.repository;

import com.service.review.domain.ReviewMessageContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewMessageContextRepository extends JpaRepository<ReviewMessageContext, Long> {
    ReviewMessageContext findTopByOrderByIdDesc();
}
