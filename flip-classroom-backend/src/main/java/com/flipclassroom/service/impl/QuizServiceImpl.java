package com.flipclassroom.service.impl;
import com.flipclassroom.dto.quiz.CreateQuizQuestionRequest;
import com.flipclassroom.dto.quiz.CreateQuizRequest;
import com.flipclassroom.dto.quiz.CreateQuizResponse;
import com.flipclassroom.dto.quiz.QuizDetailResponse;
import com.flipclassroom.dto.quiz.QuizListItemResponse;
import com.flipclassroom.dto.quiz.QuizQuestionItemResponse;
import com.flipclassroom.dto.quiz.QuizRecordItemResponse;
import com.flipclassroom.dto.quiz.SubmitQuizRequest;
import com.flipclassroom.dto.quiz.SubmitQuizResponse;
import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.CourseMember;
import com.flipclassroom.entity.PreviewQuiz;
import com.flipclassroom.entity.QuizQuestion;
import com.flipclassroom.entity.QuizRecord;
import com.flipclassroom.entity.SysUser;
import com.flipclassroom.mapper.CourseMapper;
import com.flipclassroom.mapper.QuizMapper;
import com.flipclassroom.mapper.UserMapper;
import com.flipclassroom.service.QuizService;
import com.flipclassroom.service.ScoreService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class QuizServiceImpl implements QuizService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final QuizMapper quizMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final ScoreService scoreService;

    public QuizServiceImpl(QuizMapper quizMapper, CourseMapper courseMapper, UserMapper userMapper, ScoreService scoreService) {
        this.quizMapper = quizMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.scoreService = scoreService;
    }

    @Override
    public List<QuizListItemResponse> listQuizzes(Long courseId) {
        ensureCourseExists(courseId);
        return quizMapper.findByCourseId(courseId)
                .stream()
                .map(this::toQuizListItem)
                .toList();
    }

    @Override
    public CreateQuizResponse createQuiz(Long courseId, CreateQuizRequest request) {
        ensureCourseExists(courseId);
        validateCreateQuizRequest(request);

        List<CreateQuizQuestionRequest> questions = request.getQuestions();
        int computedTotalScore = computeTotalScore(request.getTotalScore(), questions);

        PreviewQuiz quiz = new PreviewQuiz();
        quiz.setCourseId(courseId);
        quiz.setTitle(request.getTitle().trim());
        quiz.setDescription(blankToNull(request.getDescription()));
        quiz.setTotalScore(computedTotalScore);
        quiz.setStatus(normalizeStatus(request.getStatus(), 1));
        quiz.setPublishTime(blankToNull(request.getPublishTime()));

        Long quizId = quizMapper.insertQuiz(quiz);
        if (quizId == null) {
            throw new IllegalStateException("Failed to create quiz");
        }

        int questionCount = 0;
        if (questions != null && !questions.isEmpty()) {
            for (int i = 0; i < questions.size(); i++) {
                CreateQuizQuestionRequest requestQuestion = questions.get(i);
                validateQuestionRequest(requestQuestion);

                QuizQuestion question = new QuizQuestion();
                question.setQuizId(quizId);
                question.setType(normalizeQuestionType(requestQuestion.getType()));
                question.setTitle(requestQuestion.getTitle().trim());
                question.setOptionA(blankToNull(requestQuestion.getOptionA()));
                question.setOptionB(blankToNull(requestQuestion.getOptionB()));
                question.setOptionC(blankToNull(requestQuestion.getOptionC()));
                question.setOptionD(blankToNull(requestQuestion.getOptionD()));
                question.setCorrectAnswer(requestQuestion.getCorrectAnswer().trim().toUpperCase(Locale.ROOT));
                question.setScore(normalizeQuestionScore(requestQuestion.getScore(), 10));
                question.setSortOrder(requestQuestion.getSortOrder() == null ? i + 1 : requestQuestion.getSortOrder());
                quizMapper.insertQuestion(question);
                questionCount++;
            }
        }

        return new CreateQuizResponse(
                quizId,
                courseId,
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTotalScore(),
                questionCount,
                quiz.getStatus(),
                quiz.getPublishTime()
        );
    }

    @Override
    public QuizDetailResponse getQuiz(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Quiz id cannot be empty");
        }

        PreviewQuiz quiz = quizMapper.findById(id);
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz does not exist");
        }

        return new QuizDetailResponse(
                quiz.getId(),
                quiz.getCourseId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTotalScore(),
                quiz.getStatus(),
                quiz.getPublishTime()
        );
    }

    @Override
    public void updateQuizStatus(Long id, Integer status) {
        if (id == null) {
            throw new IllegalArgumentException("Quiz id cannot be empty");
        }
        Integer normalizedStatus = normalizeStatus(status, null);
        PreviewQuiz quiz = quizMapper.findById(id);
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz does not exist");
        }
        int affectedRows = quizMapper.updateQuizStatus(id, normalizedStatus);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update quiz status");
        }
    }

    @Override
    public List<QuizQuestionItemResponse> listQuizQuestions(Long quizId) {
        ensureQuizExists(quizId);
        return quizMapper.findQuestionsByQuizId(quizId)
                .stream()
                .map(this::toQuestionItem)
                .toList();
    }

    @Override
    public List<QuizRecordItemResponse> listQuizRecords(Long quizId) {
        ensureQuizExists(quizId);
        return quizMapper.findRecordsByQuizId(quizId)
                .stream()
                .map(this::toRecordItem)
                .toList();
    }

    @Override
    public SubmitQuizResponse submitQuiz(Long quizId, SubmitQuizRequest request) {
        if (request == null || request.getStudentId() == null || request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("StudentId and answers are required");
        }

        PreviewQuiz quiz = ensureQuizExists(quizId);
        if (quiz.getStatus() == null || quiz.getStatus() != 1) {
            throw new IllegalArgumentException("Quiz is not available");
        }

        SysUser student = userMapper.findById(request.getStudentId());
        if (student == null) {
            throw new IllegalArgumentException("Student does not exist");
        }
        if (student.getRole() == null || !"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new IllegalArgumentException("StudentId must point to a student user");
        }

        CourseMember courseMember = courseMapper.findMember(quiz.getCourseId(), student.getId());
        if (courseMember == null) {
            throw new IllegalArgumentException("Student is not a member of the course");
        }
        if (quizMapper.findRecord(quizId, student.getId()) != null) {
            throw new IllegalArgumentException("Student has already submitted the quiz");
        }

        List<QuizQuestion> questions = quizMapper.findQuestionsByQuizId(quizId);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Quiz has no questions");
        }

        int finalScore = calculateScore(questions, request.getAnswers());
        String answersJson = writeAnswersJson(request.getAnswers());
        String submittedAt = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        QuizRecord quizRecord = new QuizRecord();
        quizRecord.setQuizId(quizId);
        quizRecord.setStudentId(student.getId());
        quizRecord.setScore(finalScore);
        quizRecord.setStatus("SUBMITTED");
        quizRecord.setAnswersJson(answersJson);
        quizRecord.setSubmittedAt(submittedAt);

        Long recordId = quizMapper.insertRecord(quizRecord);
        if (recordId == null) {
            throw new IllegalStateException("Failed to submit quiz");
        }

        scoreService.upsertScoreRecord(
                quiz.getCourseId(),
                student.getId(),
                "quiz",
                quiz.getTitle(),
                finalScore,
                "课堂测验成绩"
        );

        return new SubmitQuizResponse(
                recordId,
                quizId,
                student.getId(),
                finalScore,
                "SUBMITTED",
                submittedAt
        );
    }

    private QuizListItemResponse toQuizListItem(PreviewQuiz quiz) {
        return new QuizListItemResponse(
                quiz.getId(),
                quiz.getCourseId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTotalScore(),
                quiz.getStatus(),
                quiz.getPublishTime()
        );
    }

    private QuizQuestionItemResponse toQuestionItem(QuizQuestion question) {
        return new QuizQuestionItemResponse(
                question.getId(),
                question.getQuizId(),
                question.getType(),
                question.getTitle(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectAnswer(),
                question.getScore(),
                question.getSortOrder()
        );
    }

    private QuizRecordItemResponse toRecordItem(QuizRecord record) {
        return new QuizRecordItemResponse(
                record.getId(),
                record.getQuizId(),
                record.getStudentId(),
                record.getStudentUsername(),
                record.getStudentName(),
                record.getScore(),
                record.getStatus(),
                record.getAnswersJson(),
                record.getSubmittedAt()
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

    private PreviewQuiz ensureQuizExists(Long quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz id cannot be empty");
        }
        PreviewQuiz quiz = quizMapper.findById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz does not exist");
        }
        return quiz;
    }

    private void validateCreateQuizRequest(CreateQuizRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getTitle())) {
            throw new IllegalArgumentException("Title is required");
        }
        normalizeStatus(request.getStatus(), 1);
        computeTotalScore(request.getTotalScore(), request.getQuestions());
    }

    private void validateQuestionRequest(CreateQuizQuestionRequest question) {
        if (question == null || isBlank(question.getTitle()) || isBlank(question.getCorrectAnswer())) {
            throw new IllegalArgumentException("Question title and correctAnswer are required");
        }
        normalizeQuestionType(question.getType());
        normalizeQuestionScore(question.getScore(), 10);
    }

    private int computeTotalScore(Integer requestedTotalScore, List<CreateQuizQuestionRequest> questions) {
        if (questions == null || questions.isEmpty()) {
            return requestedTotalScore == null ? 100 : requestedTotalScore;
        }
        int total = 0;
        for (CreateQuizQuestionRequest question : questions) {
            validateQuestionRequest(question);
            total += normalizeQuestionScore(question.getScore(), 10);
        }
        return requestedTotalScore == null ? total : requestedTotalScore;
    }

    private int calculateScore(List<QuizQuestion> questions, Map<String, String> answers) {
        int score = 0;
        for (QuizQuestion question : questions) {
            String answer = answers.get(String.valueOf(question.getId()));
            if (answer != null && question.getCorrectAnswer() != null
                    && question.getCorrectAnswer().equalsIgnoreCase(answer.trim())) {
                score += question.getScore() == null ? 0 : question.getScore();
            }
        }
        return score;
    }

    private String writeAnswersJson(Map<String, String> answers) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String key = escapeJson(entry.getKey());
            String value = escapeJson(entry.getValue());
            parts.add("\"" + key + "\":\"" + value + "\"");
        }
        return "{" + String.join(",", parts) + "}";
    }

    private Integer normalizeStatus(Integer status, Integer defaultStatus) {
        Integer finalStatus = status == null ? defaultStatus : status;
        if (finalStatus == null || (finalStatus != 0 && finalStatus != 1)) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }
        return finalStatus;
    }

    private String normalizeQuestionType(String type) {
        String finalType = isBlank(type) ? "single" : type.trim().toLowerCase(Locale.ROOT);
        if (!"single".equals(finalType)) {
            throw new IllegalArgumentException("Only single choice questions are supported for now");
        }
        return finalType;
    }

    private int normalizeQuestionScore(Integer score, int defaultScore) {
        int finalScore = score == null ? defaultScore : score;
        if (finalScore <= 0) {
            throw new IllegalArgumentException("Question score must be greater than 0");
        }
        return finalScore;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
