package com.example.learning_api.service.core;

import com.example.learning_api.dto.request.deadline.CreateDeadlineRequest;
import com.example.learning_api.dto.request.deadline.UpdateDeadlineRequest;
import com.example.learning_api.dto.response.classroom.ClassroomDeadlineResponse;
import com.example.learning_api.dto.response.deadline.GetDeadlinesResponse;
import com.example.learning_api.dto.response.deadline.UpcomingDeadlinesResponse;
import com.example.learning_api.entity.sql.database.DeadlineEntity;
import org.apache.commons.collections4.Get;

import java.util.List;

public interface IDeadlineService {
    void createDeadline(CreateDeadlineRequest createDeadlineRequest);
    void updateDeadline(UpdateDeadlineRequest updateDeadlineRequest);
    void deleteDeadline(String deadlineId);
    DeadlineEntity getDeadline(String deadlineId);
    GetDeadlinesResponse getDeadlinesByClassroomId(String classroomId, Integer page, Integer size);
    List<UpcomingDeadlinesResponse> getUpcomingDeadlineByStudentId(String studentId,String date);
    List<ClassroomDeadlineResponse> getClassroomDeadlinesByClassroomId(String classroomId);
}
