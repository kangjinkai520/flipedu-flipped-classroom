package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.sign.CreateSignTaskRequest;
import com.flipclassroom.dto.sign.CreateSignTaskResponse;
import com.flipclassroom.dto.sign.ReviewSignRecordRequest;
import com.flipclassroom.dto.sign.SignRecordItemResponse;
import com.flipclassroom.dto.sign.SignTaskDetailResponse;
import com.flipclassroom.dto.sign.SignTaskListItemResponse;
import com.flipclassroom.dto.sign.StudentSignRequest;
import com.flipclassroom.dto.sign.StudentSignResponse;
import com.flipclassroom.dto.sign.UpdateSignTaskStatusRequest;
import com.flipclassroom.service.SignService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SignController {

    private final SignService signService;

    public SignController(SignService signService) {
        this.signService = signService;
    }

    @GetMapping("/courses/{courseId}/sign-tasks")
    public Result<List<SignTaskListItemResponse>> listSignTasks(@PathVariable Long courseId) {
        try {
            return Result.success(signService.listSignTasks(courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/courses/{courseId}/sign-tasks")
    public Result<CreateSignTaskResponse> createSignTask(@PathVariable Long courseId,
                                                         @RequestBody CreateSignTaskRequest request) {
        try {
            return Result.success(signService.createSignTask(courseId, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/sign-tasks/{id}")
    public Result<SignTaskDetailResponse> getSignTask(@PathVariable Long id) {
        try {
            return Result.success(signService.getSignTask(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/sign-tasks/{id}/status")
    public Result<String> updateSignTaskStatus(@PathVariable Long id,
                                               @RequestBody UpdateSignTaskStatusRequest request) {
        try {
            signService.updateSignTaskStatus(id, request == null ? null : request.getStatus());
            return Result.success("sign task status updated");
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/sign-tasks/{id}/records")
    public Result<List<SignRecordItemResponse>> listSignRecords(@PathVariable Long id) {
        try {
            return Result.success(signService.listSignRecords(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/sign-tasks/{id}/sign")
    public Result<StudentSignResponse> submitSign(@PathVariable Long id,
                                                  @RequestBody StudentSignRequest request) {
        try {
            return Result.success(signService.submitSign(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/sign-records/{id}/review")
    public Result<String> reviewSignRecord(@PathVariable Long id,
                                           @RequestBody ReviewSignRecordRequest request) {
        try {
            signService.reviewSignRecord(id, request == null ? null : request.getStatus(), request == null ? null : request.getReviewComment());
            return Result.success("sign record reviewed");
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
