package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.feedback.CourseFeedbackItemResponse;
import com.flipclassroom.dto.feedback.CourseFeedbackSummaryResponse;
import com.flipclassroom.dto.feedback.CreateCourseFeedbackRequest;
import com.flipclassroom.service.CourseFeedbackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseFeedbackController {

    private final CourseFeedbackService feedbackService;

    public CourseFeedbackController(CourseFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/courses/{courseId}/feedback")
    public Result<CourseFeedbackItemResponse> createFeedback(@PathVariable Long courseId,
                                                             @RequestBody CreateCourseFeedbackRequest request) {
        try {
            return Result.success(feedbackService.createFeedback(courseId, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/feedback")
    public Result<List<CourseFeedbackItemResponse>> listCourseFeedback(@PathVariable Long courseId) {
        try {
            return Result.success(feedbackService.listCourseFeedback(courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/feedback/my")
    public Result<List<CourseFeedbackItemResponse>> listMyFeedback(@PathVariable Long courseId,
                                                                   @RequestParam Long studentId) {
        try {
            return Result.success(feedbackService.listMyFeedback(courseId, studentId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/feedback/summary")
    public Result<CourseFeedbackSummaryResponse> getSummary(@PathVariable Long courseId) {
        try {
            return Result.success(feedbackService.getSummary(courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
