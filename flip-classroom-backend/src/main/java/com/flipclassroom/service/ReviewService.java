package com.flipclassroom.service;

import com.flipclassroom.dto.review.CreateReviewRequest;
import com.flipclassroom.dto.review.ReviewDecisionRequest;
import com.flipclassroom.dto.review.ReviewRequestItemResponse;

import java.util.List;

public interface ReviewService {

    ReviewRequestItemResponse createReview(CreateReviewRequest request);

    List<ReviewRequestItemResponse> listReviews(String status);

    ReviewRequestItemResponse review(Long id, ReviewDecisionRequest request);
}
