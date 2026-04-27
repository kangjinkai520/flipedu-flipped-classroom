package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.notice.CreateNoticeRequest;
import com.flipclassroom.dto.notice.CreateNoticeResponse;
import com.flipclassroom.dto.notice.NoticeDetailResponse;
import com.flipclassroom.dto.notice.NoticeListItemResponse;
import com.flipclassroom.dto.notice.UpdateNoticeRequest;
import com.flipclassroom.dto.notice.UpdateNoticeResponse;
import com.flipclassroom.dto.notice.UpdateNoticeStatusRequest;
import com.flipclassroom.service.NoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/courses/{courseId}/notices")
    public Result<List<NoticeListItemResponse>> listNotices(@PathVariable Long courseId) {
        try {
            return Result.success(noticeService.listNotices(courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/courses/{courseId}/notices")
    public Result<CreateNoticeResponse> createNotice(@PathVariable Long courseId,
                                                     @RequestBody CreateNoticeRequest request) {
        try {
            return Result.success(noticeService.createNotice(courseId, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/notices/{id}")
    public Result<NoticeDetailResponse> getNotice(@PathVariable Long id) {
        try {
            return Result.success(noticeService.getNotice(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PutMapping("/notices/{id}")
    public Result<UpdateNoticeResponse> updateNotice(@PathVariable Long id,
                                                     @RequestBody UpdateNoticeRequest request) {
        try {
            return Result.success(noticeService.updateNotice(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/notices/{id}/status")
    public Result<String> updateNoticeStatus(@PathVariable Long id,
                                             @RequestBody UpdateNoticeStatusRequest request) {
        try {
            noticeService.updateNoticeStatus(id, request == null ? null : request.getStatus());
            return Result.success("notice status updated");
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
