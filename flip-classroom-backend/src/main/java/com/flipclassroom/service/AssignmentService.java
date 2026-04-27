package com.flipclassroom.service;

import com.flipclassroom.dto.assignment.AssignmentDetailResponse;
import com.flipclassroom.dto.assignment.AssignmentListItemResponse;
import com.flipclassroom.dto.assignment.AssignmentSubmissionItemResponse;
import com.flipclassroom.dto.assignment.CreateAssignmentRequest;
import com.flipclassroom.dto.assignment.CreateAssignmentResponse;
import com.flipclassroom.dto.assignment.ScoreAssignmentSubmissionRequest;
import com.flipclassroom.dto.assignment.ScoreAssignmentSubmissionResponse;
import com.flipclassroom.dto.assignment.SubmitAssignmentRequest;
import com.flipclassroom.dto.assignment.SubmitAssignmentResponse;
import com.flipclassroom.dto.assignment.UpdateAssignmentRequest;
import com.flipclassroom.dto.assignment.UpdateAssignmentResponse;

import java.util.List;

public interface AssignmentService {

    List<AssignmentListItemResponse> listAssignments(Long courseId);

    CreateAssignmentResponse createAssignment(Long courseId, CreateAssignmentRequest request);

    AssignmentDetailResponse getAssignment(Long id);

    UpdateAssignmentResponse updateAssignment(Long id, UpdateAssignmentRequest request);

    void updateAssignmentPublished(Long id, Integer published);

    List<AssignmentSubmissionItemResponse> listAssignmentSubmissions(Long assignmentId);

    SubmitAssignmentResponse submitAssignment(Long assignmentId, SubmitAssignmentRequest request);

    ScoreAssignmentSubmissionResponse scoreAssignmentSubmission(Long submissionId, ScoreAssignmentSubmissionRequest request);
}
