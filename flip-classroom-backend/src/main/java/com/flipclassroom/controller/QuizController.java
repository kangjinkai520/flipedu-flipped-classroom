package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.quiz.CreateQuizRequest;
import com.flipclassroom.dto.quiz.CreateQuizResponse;
import com.flipclassroom.dto.quiz.QuizDetailResponse;
import com.flipclassroom.dto.quiz.QuizListItemResponse;
import com.flipclassroom.dto.quiz.QuizQuestionItemResponse;
import com.flipclassroom.dto.quiz.QuizRecordItemResponse;
import com.flipclassroom.dto.quiz.SubmitQuizRequest;
import com.flipclassroom.dto.quiz.SubmitQuizResponse;
import com.flipclassroom.dto.quiz.UpdateQuizStatusRequest;
import com.flipclassroom.service.QuizService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/courses/{courseId}/quizzes")
    public Result<List<QuizListItemResponse>> listQuizzes(@PathVariable Long courseId) {
        try {
            return Result.success(quizService.listQuizzes(courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/courses/{courseId}/quizzes")
    public Result<CreateQuizResponse> createQuiz(@PathVariable Long courseId,
                                                 @RequestBody CreateQuizRequest request) {
        try {
            return Result.success(quizService.createQuiz(courseId, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/quizzes/{id}")
    public Result<QuizDetailResponse> getQuiz(@PathVariable Long id) {
        try {
            return Result.success(quizService.getQuiz(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/quizzes/{id}/status")
    public Result<String> updateQuizStatus(@PathVariable Long id,
                                           @RequestBody UpdateQuizStatusRequest request) {
        try {
            quizService.updateQuizStatus(id, request == null ? null : request.getStatus());
            return Result.success("quiz status updated");
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/quizzes/{id}/questions")
    public Result<List<QuizQuestionItemResponse>> listQuizQuestions(@PathVariable Long id) {
        try {
            return Result.success(quizService.listQuizQuestions(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/quizzes/{id}/records")
    public Result<List<QuizRecordItemResponse>> listQuizRecords(@PathVariable Long id) {
        try {
            return Result.success(quizService.listQuizRecords(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/quizzes/{id}/submit")
    public Result<SubmitQuizResponse> submitQuiz(@PathVariable Long id,
                                                 @RequestBody SubmitQuizRequest request) {
        try {
            return Result.success(quizService.submitQuiz(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
