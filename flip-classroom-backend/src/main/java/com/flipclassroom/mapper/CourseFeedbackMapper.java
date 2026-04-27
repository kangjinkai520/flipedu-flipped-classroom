package com.flipclassroom.mapper;

import com.flipclassroom.entity.CourseFeedback;

import java.util.List;

public interface CourseFeedbackMapper {

    List<CourseFeedback> findByCourseId(Long courseId);

    List<CourseFeedback> findByCourseAndStudent(Long courseId, Long studentId);

    Long insert(CourseFeedback feedback);
}
