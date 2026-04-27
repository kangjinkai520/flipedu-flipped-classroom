package com.flipclassroom.dto.score;

public class ScoreRecordItemResponse {

    private Long id;
    private Long courseId;
    private Long studentId;
    private String studentUsername;
    private String studentName;
    private String itemType;
    private String itemName;
    private Integer score;
    private String remark;

    public ScoreRecordItemResponse(Long id, Long courseId, Long studentId,
                                   String studentUsername, String studentName,
                                   String itemType, String itemName, Integer score, String remark) {
        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.studentName = studentName;
        this.itemType = itemType;
        this.itemName = itemName;
        this.score = score;
        this.remark = remark;
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getItemType() {
        return itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public Integer getScore() {
        return score;
    }

    public String getRemark() {
        return remark;
    }
}
