package com.flipclassroom.dto.quiz;

public class QuizQuestionItemResponse {

    private Long id;
    private Long quizId;
    private String type;
    private String title;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private Integer score;
    private Integer sortOrder;

    public QuizQuestionItemResponse(Long id,
                                    Long quizId,
                                    String type,
                                    String title,
                                    String optionA,
                                    String optionB,
                                    String optionC,
                                    String optionD,
                                    String correctAnswer,
                                    Integer score,
                                    Integer sortOrder) {
        this.id = id;
        this.quizId = quizId;
        this.type = type;
        this.title = title;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.score = score;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Long getQuizId() {
        return quizId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
