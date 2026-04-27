package com.flipclassroom.mapper;

import com.flipclassroom.entity.Assignment;
import com.flipclassroom.entity.AssignmentSubmit;

import java.util.List;

public interface AssignmentMapper {

    List<Assignment> findByCourseId(Long courseId);

    Assignment findById(Long id);

    Long insert(Assignment assignment);

    int updateBasicInfo(Assignment assignment);

    int updatePublished(Long id, Integer published);

    List<AssignmentSubmit> findSubmissionsByAssignmentId(Long assignmentId);

    AssignmentSubmit findSubmission(Long assignmentId, Long studentId);

    AssignmentSubmit findSubmissionById(Long id);

    Long insertSubmission(AssignmentSubmit assignmentSubmit);

    int updateSubmission(AssignmentSubmit assignmentSubmit);
}
