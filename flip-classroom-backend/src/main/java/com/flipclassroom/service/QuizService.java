package com.flipclassroom.service;

import com.flipclassroom.dto.quiz.CreateQuizRequest;
import com.flipclassroom.dto.quiz.CreateQuizResponse;
import com.flipclassroom.dto.quiz.QuizDetailResponse;
import com.flipclassroom.dto.quiz.QuizListItemResponse;
import com.flipclassroom.dto.quiz.QuizQuestionItemResponse;
import com.flipclassroom.dto.quiz.QuizRecordItemResponse;
import com.flipclassroom.dto.quiz.SubmitQuizRequest;
import com.flipclassroom.dto.quiz.SubmitQuizResponse;

import java.util.List;

public interface QuizService {

    List<QuizListItemResponse> listQuizzes(Long courseId);

    CreateQuizResponse createQuiz(Long courseId, CreateQuizRequest request);

    QuizDetailResponse getQuiz(Long id);

    void updateQuizStatus(Long id, Integer status);

    List<QuizQuestionItemResponse> listQuizQuestions(Long quizId);

    List<QuizRecordItemResponse> listQuizRecords(Long quizId);

    SubmitQuizResponse submitQuiz(Long quizId, SubmitQuizRequest request);
}
