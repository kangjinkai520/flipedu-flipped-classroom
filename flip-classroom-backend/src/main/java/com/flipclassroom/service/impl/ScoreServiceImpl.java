package com.flipclassroom.service.impl;

import com.flipclassroom.dto.score.ScoreRecordItemResponse;
import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.ScoreRecord;
import com.flipclassroom.entity.SysUser;
import com.flipclassroom.mapper.CourseMapper;
import com.flipclassroom.mapper.ScoreMapper;
import com.flipclassroom.mapper.UserMapper;
import com.flipclassroom.service.ScoreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public ScoreServiceImpl(ScoreMapper scoreMapper, CourseMapper courseMapper, UserMapper userMapper) {
        this.scoreMapper = scoreMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<ScoreRecordItemResponse> listCourseScores(Long courseId) {
        ensureCourseExists(courseId);
        return scoreMapper.findByCourseId(courseId)
                .stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public List<ScoreRecordItemResponse> listUserScores(Long userId, Long courseId) {
        ensureUserExists(userId);
        List<ScoreRecord> scoreRecords;
        if (courseId == null) {
            scoreRecords = scoreMapper.findByStudentId(userId);
        } else {
            ensureCourseExists(courseId);
            scoreRecords = scoreMapper.findByStudentIdAndCourseId(userId, courseId);
        }
        return scoreRecords.stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public void upsertScoreRecord(Long courseId, Long studentId, String itemType, String itemName, Integer score, String remark) {
        ensureCourseExists(courseId);
        SysUser student = ensureUserExists(studentId);
        if (student.getRole() == null || !"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new IllegalArgumentException("StudentId must point to a student user");
        }
        if (isBlank(itemType) || isBlank(itemName) || score == null || score < 0) {
            throw new IllegalArgumentException("itemType, itemName and score are required");
        }

        String normalizedItemType = itemType.trim().toLowerCase(Locale.ROOT);
        String normalizedItemName = itemName.trim();
        ScoreRecord existingRecord = scoreMapper.findByItem(courseId, studentId, normalizedItemType, normalizedItemName);

        if (existingRecord == null) {
            ScoreRecord scoreRecord = new ScoreRecord();
            scoreRecord.setCourseId(courseId);
            scoreRecord.setStudentId(studentId);
            scoreRecord.setItemType(normalizedItemType);
            scoreRecord.setItemName(normalizedItemName);
            scoreRecord.setScore(score);
            scoreRecord.setRemark(blankToNull(remark));
            Long id = scoreMapper.insert(scoreRecord);
            if (id == null) {
                throw new IllegalStateException("Failed to create score record");
            }
            return;
        }

        existingRecord.setCourseId(courseId);
        existingRecord.setStudentId(studentId);
        existingRecord.setItemType(normalizedItemType);
        existingRecord.setItemName(normalizedItemName);
        existingRecord.setScore(score);
        existingRecord.setRemark(blankToNull(remark));
        int affectedRows = scoreMapper.update(existingRecord);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update score record");
        }
    }

    private ScoreRecordItemResponse toItem(ScoreRecord scoreRecord) {
        return new ScoreRecordItemResponse(
                scoreRecord.getId(),
                scoreRecord.getCourseId(),
                scoreRecord.getStudentId(),
                scoreRecord.getStudentUsername(),
                scoreRecord.getStudentName(),
                scoreRecord.getItemType(),
                scoreRecord.getItemName(),
                scoreRecord.getScore(),
                scoreRecord.getRemark()
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

    private SysUser ensureUserExists(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be empty");
        }
        SysUser user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        return user;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
