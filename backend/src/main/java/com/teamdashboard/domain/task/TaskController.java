package com.teamdashboard.domain.task;

import com.teamdashboard.common.ApiResponse;
import com.teamdashboard.domain.task.dto.CreateTaskRequest;
import com.teamdashboard.domain.task.dto.TaskResponse;
import com.teamdashboard.domain.task.dto.UpdateTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long projectId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TaskResponse> tasks = taskService.getTasks(status, priority, assigneeId, projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long id) {
        TaskResponse task = taskService.getTask(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        TaskResponse task = taskService.createTask(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(task));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        TaskResponse task = taskService.updateTask(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        taskService.deleteTask(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
