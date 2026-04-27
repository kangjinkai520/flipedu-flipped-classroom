package com.flipclassroom.service.impl;

import com.flipclassroom.dto.notice.CreateNoticeRequest;
import com.flipclassroom.dto.notice.CreateNoticeResponse;
import com.flipclassroom.dto.notice.NoticeDetailResponse;
import com.flipclassroom.dto.notice.NoticeListItemResponse;
import com.flipclassroom.dto.notice.UpdateNoticeRequest;
import com.flipclassroom.dto.notice.UpdateNoticeResponse;
import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.Notice;
import com.flipclassroom.mapper.CourseMapper;
import com.flipclassroom.mapper.NoticeMapper;
import com.flipclassroom.service.NoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final CourseMapper courseMapper;

    public NoticeServiceImpl(NoticeMapper noticeMapper, CourseMapper courseMapper) {
        this.noticeMapper = noticeMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public List<NoticeListItemResponse> listNotices(Long courseId) {
        ensureCourseExists(courseId);
        return noticeMapper.findByCourseId(courseId)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public CreateNoticeResponse createNotice(Long courseId, CreateNoticeRequest request) {
        ensureCourseExists(courseId);
        validateCreateRequest(request);

        Notice notice = new Notice();
        notice.setCourseId(courseId);
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        notice.setPublishTime(normalizePublishTime(request.getPublishTime()));
        notice.setStatus(normalizeStatus(request.getStatus(), 1));

        Long id = noticeMapper.insert(notice);
        if (id == null) {
            throw new IllegalStateException("Failed to create notice");
        }

        return new CreateNoticeResponse(
                id,
                notice.getCourseId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getPublishTime(),
                notice.getStatus()
        );
    }

    @Override
    public NoticeDetailResponse getNotice(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Notice id cannot be empty");
        }

        Notice notice = noticeMapper.findById(id);
        if (notice == null) {
            throw new IllegalArgumentException("Notice does not exist");
        }

        return toDetail(notice);
    }

    @Override
    public UpdateNoticeResponse updateNotice(Long id, UpdateNoticeRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Notice id cannot be empty");
        }
        validateUpdateRequest(request);

        Notice existingNotice = noticeMapper.findById(id);
        if (existingNotice == null) {
            throw new IllegalArgumentException("Notice does not exist");
        }

        ensureCourseExists(existingNotice.getCourseId());

        existingNotice.setTitle(request.getTitle().trim());
        existingNotice.setContent(request.getContent().trim());
        existingNotice.setPublishTime(normalizePublishTime(request.getPublishTime()));
        existingNotice.setStatus(normalizeStatus(request.getStatus(), existingNotice.getStatus()));

        int affectedRows = noticeMapper.updateBasicInfo(existingNotice);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update notice");
        }

        return new UpdateNoticeResponse(
                existingNotice.getId(),
                existingNotice.getCourseId(),
                existingNotice.getTitle(),
                existingNotice.getContent(),
                existingNotice.getPublishTime(),
                existingNotice.getStatus()
        );
    }

    @Override
    public void updateNoticeStatus(Long id, Integer status) {
        if (id == null) {
            throw new IllegalArgumentException("Notice id cannot be empty");
        }
        Integer normalizedStatus = normalizeStatus(status, null);
        Notice notice = noticeMapper.findById(id);
        if (notice == null) {
            throw new IllegalArgumentException("Notice does not exist");
        }
        int affectedRows = noticeMapper.updateStatus(id, normalizedStatus);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update notice status");
        }
    }

    private NoticeListItemResponse toListItem(Notice notice) {
        return new NoticeListItemResponse(
                notice.getId(),
                notice.getCourseId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getPublishTime(),
                notice.getStatus()
        );
    }

    private NoticeDetailResponse toDetail(Notice notice) {
        return new NoticeDetailResponse(
                notice.getId(),
                notice.getCourseId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getPublishTime(),
                notice.getStatus()
        );
    }

    private Course ensureCourseExists(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("Course id cannot be empty");
        }
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course does not exist");
        }
        return course;
    }

    private void validateCreateRequest(CreateNoticeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getTitle()) || isBlank(request.getContent())) {
            throw new IllegalArgumentException("Title and content are required");
        }
        normalizeStatus(request.getStatus(), 1);
        normalizePublishTime(request.getPublishTime());
    }

    private void validateUpdateRequest(UpdateNoticeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getTitle()) || isBlank(request.getContent())) {
            throw new IllegalArgumentException("Title and content are required");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        normalizeStatus(request.getStatus(), 1);
        normalizePublishTime(request.getPublishTime());
    }

    private Integer normalizeStatus(Integer status, Integer defaultStatus) {
        Integer finalStatus = status == null ? defaultStatus : status;
        if (finalStatus == null || (finalStatus != 0 && finalStatus != 1)) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }
        return finalStatus;
    }

    private String normalizePublishTime(String publishTime) {
        return isBlank(publishTime) ? null : publishTime.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
