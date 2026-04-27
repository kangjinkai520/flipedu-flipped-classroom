package com.flipclassroom.service;

import com.flipclassroom.dto.sign.CreateSignTaskRequest;
import com.flipclassroom.dto.sign.CreateSignTaskResponse;
import com.flipclassroom.dto.sign.SignRecordItemResponse;
import com.flipclassroom.dto.sign.SignTaskDetailResponse;
import com.flipclassroom.dto.sign.SignTaskListItemResponse;
import com.flipclassroom.dto.sign.StudentSignRequest;
import com.flipclassroom.dto.sign.StudentSignResponse;

import java.util.List;

public interface SignService {

    List<SignTaskListItemResponse> listSignTasks(Long courseId);

    CreateSignTaskResponse createSignTask(Long courseId, CreateSignTaskRequest request);

    SignTaskDetailResponse getSignTask(Long id);

    void updateSignTaskStatus(Long id, String status);

    List<SignRecordItemResponse> listSignRecords(Long signTaskId);

    StudentSignResponse submitSign(Long signTaskId, StudentSignRequest request);

    void reviewSignRecord(Long recordId, String status, String reviewComment);
}
