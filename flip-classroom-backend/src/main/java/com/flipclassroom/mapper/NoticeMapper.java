package com.flipclassroom.mapper;

import com.flipclassroom.entity.Notice;

import java.util.List;

public interface NoticeMapper {

    List<Notice> findByCourseId(Long courseId);

    Notice findById(Long id);

    Long insert(Notice notice);

    int updateBasicInfo(Notice notice);

    int updateStatus(Long id, Integer status);
}
