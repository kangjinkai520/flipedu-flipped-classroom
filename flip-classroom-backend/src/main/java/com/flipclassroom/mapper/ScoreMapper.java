package com.flipclassroom.mapper;

import com.flipclassroom.entity.ScoreRecord;

import java.util.List;

public interface ScoreMapper {

    List<ScoreRecord> findByCourseId(Long courseId);

    List<ScoreRecord> findByStudentId(Long studentId);

    List<ScoreRecord> findByStudentIdAndCourseId(Long studentId, Long courseId);

    ScoreRecord findByItem(Long courseId, Long studentId, String itemType, String itemName);

    Long insert(ScoreRecord scoreRecord);

    int update(ScoreRecord scoreRecord);
}
