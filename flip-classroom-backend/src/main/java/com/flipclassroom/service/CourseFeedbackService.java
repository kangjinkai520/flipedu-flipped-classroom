package com.flipclassroom.service;

import com.flipclassroom.dto.feedback.CourseFeedbackItemResponse;
import com.flipclassroom.dto.feedback.CourseFeedbackSummaryResponse;
import com.flipclassroom.dto.feedback.CreateCourseFeedbackRequest;

import java.util.List;

public interface CourseFeedbackService {

    CourseFeedbackItemResponse createFeedback(Long courseId, CreateCourseFeedbackRequest request);

    List<CourseFeedbackItemResponse> listCourseFeedback(Long courseId);

    List<CourseFeedbackItemResponse> listMyFeedback(Long courseId, Long studentId);

    CourseFeedbackSummaryResponse getSummary(Long courseId);
}
