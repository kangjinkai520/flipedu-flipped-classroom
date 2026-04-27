package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.review.CreateReviewRequest;
import com.flipclassroom.dto.review.ReviewDecisionRequest;
import com.flipclassroom.dto.review.ReviewRequestItemResponse;
import com.flipclassroom.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public Result<ReviewRequestItemResponse> createReview(@RequestBody CreateReviewRequest request) {
        try {
            return Result.success(reviewService.createReview(request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/admin/reviews")
    public Result<List<ReviewRequestItemResponse>> listReviews(@RequestParam(required = false) String status) {
        try {
            return Result.success(reviewService.listReviews(status));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/admin/reviews/{id}")
    public Result<ReviewRequestItemResponse> review(@PathVariable Long id,
                                                    @RequestBody ReviewDecisionRequest request) {
        try {
            return Result.success(reviewService.review(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
