package com.teamdashboard.domain.task;

import com.teamdashboard.common.AppException;
import com.teamdashboard.domain.project.Project;
import com.teamdashboard.domain.project.ProjectRepository;
import com.teamdashboard.domain.task.dto.CreateTaskRequest;
import com.teamdashboard.domain.task.dto.TaskResponse;
import com.teamdashboard.domain.task.dto.UpdateTaskRequest;
import com.teamdashboard.domain.user.User;
import com.teamdashboard.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public Page<TaskResponse> getTasks(TaskStatus status, TaskPriority priority, Long assigneeId, Long projectId,
                                       Pageable pageable) {
        Specification<Task> spec = TaskSpecification.withFilters(status, priority, assigneeId, projectId);
        return taskRepository.findAll(spec, pageable).map(TaskResponse::from);
    }

    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findByIdWithAssigneeAndProject(id)
                .orElseThrow(() -> AppException.notFound("태스크를 찾을 수 없습니다. id=" + id));
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, Long currentUserId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> AppException.notFound("프로젝트를 찾을 수 없습니다. id=" + request.getProjectId()));

        if (!project.getOwner().getId().equals(currentUserId)) {
            throw AppException.forbidden("이 프로젝트에 태스크를 생성할 권한이 없습니다");
        }

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> AppException.notFound("담당자를 찾을 수 없습니다. id=" + request.getAssigneeId()));

        Task task = Task.builder()
                .title(request.getTitle())
                .status(request.getStatus())
                .priority(request.getPriority())
                .assignee(assignee)
                .project(project)
                .build();

        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request, Long currentUserId) {
        Task task = taskRepository.findByIdWithAssigneeAndProject(id)
                .orElseThrow(() -> AppException.notFound("태스크를 찾을 수 없습니다. id=" + id));

        checkTaskPermission(task, currentUserId);

        if (request.getTitle() != null) {
            task.updateTitle(request.getTitle());
        }
        if (request.getStatus() != null) {
            task.changeStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.changePriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> AppException.notFound("담당자를 찾을 수 없습니다. id=" + request.getAssigneeId()));
            task.reassign(assignee);
        }

        return TaskResponse.from(task);
    }

    @Transactional
    public void deleteTask(Long id, Long currentUserId) {
        Task task = taskRepository.findByIdWithAssigneeAndProject(id)
                .orElseThrow(() -> AppException.notFound("태스크를 찾을 수 없습니다. id=" + id));

        checkTaskPermission(task, currentUserId);

        taskRepository.delete(task);
    }

    private void checkTaskPermission(Task task, Long currentUserId) {
        boolean isProjectOwner = task.getProject().getOwner().getId().equals(currentUserId);
        boolean isAssignee = task.getAssignee().getId().equals(currentUserId);

        if (!isProjectOwner && !isAssignee) {
            throw AppException.forbidden("이 태스크를 수정/삭제할 권한이 없습니다");
        }
    }
}
