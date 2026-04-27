package com.flipclassroom.service;

import com.flipclassroom.dto.score.ScoreRecordItemResponse;

import java.util.List;

public interface ScoreService {

    List<ScoreRecordItemResponse> listCourseScores(Long courseId);

    List<ScoreRecordItemResponse> listUserScores(Long userId, Long courseId);

    void upsertScoreRecord(Long courseId, Long studentId, String itemType, String itemName, Integer score, String remark);
}
