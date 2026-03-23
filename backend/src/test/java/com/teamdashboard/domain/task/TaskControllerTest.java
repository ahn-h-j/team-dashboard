package com.teamdashboard.domain.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamdashboard.common.AppException;
import com.teamdashboard.config.JwtAuthenticationFilter;
import com.teamdashboard.config.JwtTokenProvider;
import com.teamdashboard.config.SecurityConfig;
import com.teamdashboard.domain.task.dto.CreateTaskRequest;
import com.teamdashboard.domain.task.dto.TaskResponse;
import com.teamdashboard.domain.task.dto.UpdateTaskRequest;
import com.teamdashboard.global.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final Long TEST_USER_ID = 1L;

    private UsernamePasswordAuthenticationToken authToken() {
        return new UsernamePasswordAuthenticationToken(
                TEST_USER_ID, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private TaskResponse sampleTaskResponse() {
        return TaskResponse.builder()
                .id(1L)
                .title("Sample Task")
                .status("TODO")
                .priority("HIGH")
                .assigneeId(1L)
                .assigneeName("Test User")
                .projectId(1L)
                .projectName("Test Project")
                .createdAt(LocalDateTime.of(2026, 3, 23, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 23, 10, 0))
                .build();
    }

    // ====================================================================
    // Authentication Tests - 인증 없는 요청 → 401
    // ====================================================================
    @Nested
    @DisplayName("인증되지 않은 요청일 때")
    class UnauthorizedTests {

        @Test
        @DisplayName("GET /api/tasks 요청 시 401을 반환해야 한다")
        void getTasks_without_auth_returns_401() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("POST /api/tasks 요청 시 401을 반환해야 한다")
        void createTask_without_auth_returns_401() throws Exception {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .assigneeId(1L)
                    .projectId(1L)
                    .build();

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PATCH /api/tasks/1 요청 시 401을 반환해야 한다")
        void updateTask_without_auth_returns_401() throws Exception {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated")
                    .build();

            mockMvc.perform(patch("/api/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/tasks/1 요청 시 401을 반환해야 한다")
        void deleteTask_without_auth_returns_401() throws Exception {
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ====================================================================
    // Validation (400) Tests
    // ====================================================================
    @Nested
    @DisplayName("유효성 검증 실패일 때")
    class ValidationTests {

        @Test
        @DisplayName("POST /api/tasks 에 빈 제목을 보내면 400을 반환해야 한다")
        void createTask_blank_title_returns_400() throws Exception {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .assigneeId(1L)
                    .projectId(1L)
                    .build();

            mockMvc.perform(post("/api/tasks")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("POST /api/tasks 에 status가 null이면 400을 반환해야 한다")
        void createTask_null_status_returns_400() throws Exception {
            String json = """
                    {"title": "Task", "priority": "HIGH", "assigneeId": 1, "projectId": 1}
                    """;

            mockMvc.perform(post("/api/tasks")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/tasks 에 priority가 null이면 400을 반환해야 한다")
        void createTask_null_priority_returns_400() throws Exception {
            String json = """
                    {"title": "Task", "status": "TODO", "assigneeId": 1, "projectId": 1}
                    """;

            mockMvc.perform(post("/api/tasks")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/tasks 에 assigneeId가 null이면 400을 반환해야 한다")
        void createTask_null_assigneeId_returns_400() throws Exception {
            String json = """
                    {"title": "Task", "status": "TODO", "priority": "HIGH", "projectId": 1}
                    """;

            mockMvc.perform(post("/api/tasks")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/tasks 에 projectId가 null이면 400을 반환해야 한다")
        void createTask_null_projectId_returns_400() throws Exception {
            String json = """
                    {"title": "Task", "status": "TODO", "priority": "HIGH", "assigneeId": 1}
                    """;

            mockMvc.perform(post("/api/tasks")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("PATCH /api/tasks/1 에 빈 문자열 제목을 보내면 400을 반환해야 한다")
        void updateTask_empty_title_returns_400() throws Exception {
            String json = """
                    {"title": ""}
                    """;

            mockMvc.perform(patch("/api/tasks/1")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/tasks 에 잘못된 status 값을 보내면 400을 반환해야 한다")
        void createTask_invalid_status_returns_400() throws Exception {
            String json = """
                    {"title": "Task", "status": "INVALID", "priority": "HIGH", "assigneeId": 1, "projectId": 1}
                    """;

            mockMvc.perform(post("/api/tasks")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/tasks 에 잘못된 priority 값을 보내면 400을 반환해야 한다")
        void createTask_invalid_priority_returns_400() throws Exception {
            String json = """
                    {"title": "Task", "status": "TODO", "priority": "INVALID", "assigneeId": 1, "projectId": 1}
                    """;

            mockMvc.perform(post("/api/tasks")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ====================================================================
    // Success Cases
    // ====================================================================
    @Nested
    @DisplayName("정상 요청일 때")
    class SuccessTests {

        @Test
        @DisplayName("GET /api/tasks 요청 시 200과 페이지네이션 응답을 반환해야 한다")
        void getTasks_returns_200_with_paginated_response() throws Exception {
            TaskResponse task = sampleTaskResponse();
            Page<TaskResponse> page = new PageImpl<>(List.of(task), PageRequest.of(0, 20), 1);

            given(taskService.getTasks(any(), any(), any(), any(), any())).willReturn(page);

            mockMvc.perform(get("/api/tasks")
                            .with(authentication(authToken())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Sample Task"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("GET /api/tasks/1 요청 시 200과 태스크 응답을 반환해야 한다")
        void getTask_returns_200_with_task_response() throws Exception {
            given(taskService.getTask(1L)).willReturn(sampleTaskResponse());

            mockMvc.perform(get("/api/tasks/1")
                            .with(authentication(authToken())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("Sample Task"))
                    .andExpect(jsonPath("$.data.status").value("TODO"))
                    .andExpect(jsonPath("$.data.priority").value("HIGH"))
                    .andExpect(jsonPath("$.data.assigneeId").value(1))
                    .andExpect(jsonPath("$.data.projectId").value(1));
        }

        @Test
        @DisplayName("POST /api/tasks 에 유효한 데이터를 보내면 201을 반환해야 한다")
        void createTask_returns_201() throws Exception {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("New Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .assigneeId(1L)
                    .projectId(1L)
                    .build();

            given(taskService.createTask(any(CreateTaskRequest.class), eq(TEST_USER_ID)))
                    .willReturn(sampleTaskResponse());

            mockMvc.perform(post("/api/tasks")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("PATCH /api/tasks/1 에 부분 업데이트 데이터를 보내면 200을 반환해야 한다")
        void updateTask_returns_200() throws Exception {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated Task")
                    .build();

            TaskResponse updatedResponse = TaskResponse.builder()
                    .id(1L)
                    .title("Updated Task")
                    .status("TODO")
                    .priority("HIGH")
                    .assigneeId(1L)
                    .assigneeName("Test User")
                    .projectId(1L)
                    .projectName("Test Project")
                    .createdAt(LocalDateTime.of(2026, 3, 23, 10, 0))
                    .updatedAt(LocalDateTime.of(2026, 3, 23, 11, 0))
                    .build();

            given(taskService.updateTask(eq(1L), any(UpdateTaskRequest.class), eq(TEST_USER_ID)))
                    .willReturn(updatedResponse);

            mockMvc.perform(patch("/api/tasks/1")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Updated Task"));
        }

        @Test
        @DisplayName("DELETE /api/tasks/1 요청 시 200을 반환해야 한다")
        void deleteTask_returns_200() throws Exception {
            doNothing().when(taskService).deleteTask(eq(1L), eq(TEST_USER_ID));

            mockMvc.perform(delete("/api/tasks/1")
                            .with(authentication(authToken()))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ====================================================================
    // Authorization (403) Tests
    // ====================================================================
    @Nested
    @DisplayName("권한이 없는 사용자가 요청할 때")
    class ForbiddenTests {

        @Test
        @DisplayName("PATCH /api/tasks/1 요청 시 403을 반환해야 한다")
        void updateTask_forbidden_returns_403() throws Exception {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated")
                    .build();

            given(taskService.updateTask(eq(1L), any(UpdateTaskRequest.class), eq(TEST_USER_ID)))
                    .willThrow(AppException.forbidden("해당 태스크를 수정할 권한이 없습니다."));

            mockMvc.perform(patch("/api/tasks/1")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("DELETE /api/tasks/1 요청 시 403을 반환해야 한다")
        void deleteTask_forbidden_returns_403() throws Exception {
            doThrow(AppException.forbidden("해당 태스크를 삭제할 권한이 없습니다."))
                    .when(taskService).deleteTask(eq(1L), eq(TEST_USER_ID));

            mockMvc.perform(delete("/api/tasks/1")
                            .with(authentication(authToken()))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    // ====================================================================
    // Not Found (404) Tests
    // ====================================================================
    @Nested
    @DisplayName("존재하지 않는 태스크를 요청할 때")
    class NotFoundTests {

        @Test
        @DisplayName("GET /api/tasks/999 요청 시 404를 반환해야 한다")
        void getTask_notFound_returns_404() throws Exception {
            given(taskService.getTask(999L))
                    .willThrow(AppException.notFound("태스크를 찾을 수 없습니다. id=999"));

            mockMvc.perform(get("/api/tasks/999")
                            .with(authentication(authToken())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("태스크를 찾을 수 없습니다. id=999"));
        }

        @Test
        @DisplayName("PATCH /api/tasks/999 요청 시 404를 반환해야 한다")
        void updateTask_notFound_returns_404() throws Exception {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated")
                    .build();

            given(taskService.updateTask(eq(999L), any(UpdateTaskRequest.class), eq(TEST_USER_ID)))
                    .willThrow(AppException.notFound("태스크를 찾을 수 없습니다. id=999"));

            mockMvc.perform(patch("/api/tasks/999")
                            .with(authentication(authToken()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("태스크를 찾을 수 없습니다. id=999"));
        }

        @Test
        @DisplayName("DELETE /api/tasks/999 요청 시 404를 반환해야 한다")
        void deleteTask_notFound_returns_404() throws Exception {
            doThrow(AppException.notFound("태스크를 찾을 수 없습니다. id=999"))
                    .when(taskService).deleteTask(eq(999L), eq(TEST_USER_ID));

            mockMvc.perform(delete("/api/tasks/999")
                            .with(authentication(authToken()))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("태스크를 찾을 수 없습니다. id=999"));
        }
    }
}
