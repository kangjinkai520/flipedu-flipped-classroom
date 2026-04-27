package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.score.ScoreRecordItemResponse;
import com.flipclassroom.service.ScoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @GetMapping("/courses/{courseId}/scores")
    public Result<List<ScoreRecordItemResponse>> listCourseScores(@PathVariable Long courseId) {
        try {
            return Result.success(scoreService.listCourseScores(courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/users/{userId}/scores")
    public Result<List<ScoreRecordItemResponse>> listUserScores(@PathVariable Long userId,
                                                                @RequestParam(required = false) Long courseId) {
        try {
            return Result.success(scoreService.listUserScores(userId, courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
