package com.teamdashboard.domain.task.dto;

import com.teamdashboard.domain.task.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String status;
    private String priority;
    private Long assigneeId;
    private String assigneeName;
    private Long projectId;
    private String projectName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .assigneeId(task.getAssignee().getId())
                .assigneeName(task.getAssignee().getName())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
