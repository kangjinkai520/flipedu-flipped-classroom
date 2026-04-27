package com.flipclassroom.mapper;

import com.flipclassroom.entity.PreviewQuiz;
import com.flipclassroom.entity.QuizQuestion;
import com.flipclassroom.entity.QuizRecord;

import java.util.List;

public interface QuizMapper {

    List<PreviewQuiz> findByCourseId(Long courseId);

    PreviewQuiz findById(Long id);

    Long insertQuiz(PreviewQuiz quiz);

    int updateQuizStatus(Long id, Integer status);

    List<QuizQuestion> findQuestionsByQuizId(Long quizId);

    void insertQuestion(QuizQuestion question);

    List<QuizRecord> findRecordsByQuizId(Long quizId);

    QuizRecord findRecord(Long quizId, Long studentId);

    Long insertRecord(QuizRecord quizRecord);
}
