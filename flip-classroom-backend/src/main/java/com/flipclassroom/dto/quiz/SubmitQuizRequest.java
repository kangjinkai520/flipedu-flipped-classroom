package com.flipclassroom.dto.quiz;

import java.util.Map;

public class SubmitQuizRequest {

    private Long studentId;
    private Map<String, String> answers;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }
}
