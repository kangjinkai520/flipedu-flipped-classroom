package com.flipclassroom.service;

import com.flipclassroom.dto.notice.CreateNoticeRequest;
import com.flipclassroom.dto.notice.CreateNoticeResponse;
import com.flipclassroom.dto.notice.NoticeDetailResponse;
import com.flipclassroom.dto.notice.NoticeListItemResponse;
import com.flipclassroom.dto.notice.UpdateNoticeRequest;
import com.flipclassroom.dto.notice.UpdateNoticeResponse;

import java.util.List;

public interface NoticeService {

    List<NoticeListItemResponse> listNotices(Long courseId);

    CreateNoticeResponse createNotice(Long courseId, CreateNoticeRequest request);

    NoticeDetailResponse getNotice(Long id);

    UpdateNoticeResponse updateNotice(Long id, UpdateNoticeRequest request);

    void updateNoticeStatus(Long id, Integer status);
}
