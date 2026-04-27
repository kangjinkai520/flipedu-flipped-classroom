package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.assignment.AssignmentDetailResponse;
import com.flipclassroom.dto.assignment.AssignmentListItemResponse;
import com.flipclassroom.dto.assignment.AssignmentSubmissionItemResponse;
import com.flipclassroom.dto.assignment.CreateAssignmentRequest;
import com.flipclassroom.dto.assignment.CreateAssignmentResponse;
import com.flipclassroom.dto.assignment.ScoreAssignmentSubmissionRequest;
import com.flipclassroom.dto.assignment.ScoreAssignmentSubmissionResponse;
import com.flipclassroom.dto.assignment.SubmitAssignmentRequest;
import com.flipclassroom.dto.assignment.SubmitAssignmentResponse;
import com.flipclassroom.dto.assignment.UpdateAssignmentPublishedRequest;
import com.flipclassroom.dto.assignment.UpdateAssignmentRequest;
import com.flipclassroom.dto.assignment.UpdateAssignmentResponse;
import com.flipclassroom.service.AssignmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/courses/{courseId}/assignments")
    public Result<List<AssignmentListItemResponse>> listAssignments(@PathVariable Long courseId) {
        try {
            return Result.success(assignmentService.listAssignments(courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/courses/{courseId}/assignments")
    public Result<CreateAssignmentResponse> createAssignment(@PathVariable Long courseId,
                                                             @RequestBody CreateAssignmentRequest request) {
        try {
            return Result.success(assignmentService.createAssignment(courseId, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/assignments/{id}")
    public Result<AssignmentDetailResponse> getAssignment(@PathVariable Long id) {
        try {
            return Result.success(assignmentService.getAssignment(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PutMapping("/assignments/{id}")
    public Result<UpdateAssignmentResponse> updateAssignment(@PathVariable Long id,
                                                             @RequestBody UpdateAssignmentRequest request) {
        try {
            return Result.success(assignmentService.updateAssignment(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/assignments/{id}/published")
    public Result<String> updateAssignmentPublished(@PathVariable Long id,
                                                    @RequestBody UpdateAssignmentPublishedRequest request) {
        try {
            assignmentService.updateAssignmentPublished(id, request == null ? null : request.getPublished());
            return Result.success("assignment published updated");
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/assignments/{id}/submissions")
    public Result<List<AssignmentSubmissionItemResponse>> listAssignmentSubmissions(@PathVariable Long id) {
        try {
            return Result.success(assignmentService.listAssignmentSubmissions(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/assignments/{id}/submit")
    public Result<SubmitAssignmentResponse> submitAssignment(@PathVariable Long id,
                                                             @RequestBody SubmitAssignmentRequest request) {
        try {
            return Result.success(assignmentService.submitAssignment(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/assignment-submissions/{id}/score")
    public Result<ScoreAssignmentSubmissionResponse> scoreAssignmentSubmission(@PathVariable Long id,
                                                                               @RequestBody ScoreAssignmentSubmissionRequest request) {
        try {
            return Result.success(assignmentService.scoreAssignmentSubmission(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
