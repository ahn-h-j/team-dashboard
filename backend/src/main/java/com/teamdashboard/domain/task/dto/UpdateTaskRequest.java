package com.teamdashboard.domain.task.dto;

import com.teamdashboard.domain.task.TaskPriority;
import com.teamdashboard.domain.task.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이내여야 합니다")
    private String title;

    private TaskStatus status;
    private TaskPriority priority;
    private Long assigneeId;
}
