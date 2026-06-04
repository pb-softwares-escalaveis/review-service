package com.service.review.repository;

import com.service.review.domain.ReviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewMessageRepository extends JpaRepository<ReviewMessage, Long> {
}
