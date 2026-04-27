package com.flipclassroom.mapper;

import com.flipclassroom.entity.SignRecord;
import com.flipclassroom.entity.SignTask;

import java.util.List;

public interface SignMapper {

    List<SignTask> findTasksByCourseId(Long courseId);

    SignTask findTaskById(Long id);

    Long insertTask(SignTask signTask);

    int updateTaskStatus(Long id, String status);

    List<SignRecord> findRecordsByTaskId(Long signTaskId);

    SignRecord findRecord(Long signTaskId, Long studentId);

    SignRecord findRecordById(Long id);

    Long insertRecord(SignRecord signRecord);

    int updateRecordReview(Long id, String status, String reviewComment);
}
