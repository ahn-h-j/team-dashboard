package com.teamdashboard.domain.task.dto;

import com.teamdashboard.domain.task.TaskPriority;
import com.teamdashboard.domain.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다")
    private String title;

    @NotNull(message = "상태는 필수입니다")
    private TaskStatus status;

    @NotNull(message = "우선순위는 필수입니다")
    private TaskPriority priority;

    @NotNull(message = "담당자는 필수입니다")
    private Long assigneeId;

    @NotNull(message = "프로젝트는 필수입니다")
    private Long projectId;
}
