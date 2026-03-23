package com.teamdashboard.domain.task;

import com.teamdashboard.common.AppException;
import com.teamdashboard.domain.project.Project;
import com.teamdashboard.domain.project.ProjectRepository;
import com.teamdashboard.domain.task.dto.CreateTaskRequest;
import com.teamdashboard.domain.task.dto.TaskResponse;
import com.teamdashboard.domain.task.dto.UpdateTaskRequest;
import com.teamdashboard.domain.user.User;
import com.teamdashboard.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    private User projectOwner;
    private User assignee;
    private User otherUser;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        projectOwner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .name("Owner")
                .password("encoded")
                .role("MEMBER")
                .build();

        assignee = User.builder()
                .id(2L)
                .email("assignee@test.com")
                .name("Assignee")
                .password("encoded")
                .role("MEMBER")
                .build();

        otherUser = User.builder()
                .id(3L)
                .email("other@test.com")
                .name("Other")
                .password("encoded")
                .role("MEMBER")
                .build();

        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("desc")
                .owner(projectOwner)
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assignee(assignee)
                .project(testProject)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getTasks")
    class GetTasks {

        @Test
        @DisplayName("필터 없이 태스크 목록을 조회한다")
        void getTasksWithoutFilter() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Task> taskPage = new PageImpl<>(List.of(testTask), pageable, 1);
            given(taskRepository.findAll(any(Specification.class), eq(pageable))).willReturn(taskPage);

            Page<TaskResponse> result = taskService.getTasks(null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Task");
        }

        @Test
        @DisplayName("필터를 적용하여 태스크 목록을 조회한다")
        void getTasksWithFilter() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Task> taskPage = new PageImpl<>(List.of(testTask), pageable, 1);
            given(taskRepository.findAll(any(Specification.class), eq(pageable))).willReturn(taskPage);

            Page<TaskResponse> result = taskService.getTasks(TaskStatus.TODO, TaskPriority.HIGH, 2L, 1L, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(taskRepository).findAll(any(Specification.class), eq(pageable));
        }
    }

    @Nested
    @DisplayName("getTask")
    class GetTask {

        @Test
        @DisplayName("ID로 태스크를 조회한다")
        void getTaskById() {
            given(taskRepository.findByIdWithAssigneeAndProject(1L)).willReturn(Optional.of(testTask));

            TaskResponse result = taskService.getTask(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getStatus()).isEqualTo("TODO");
            assertThat(result.getPriority()).isEqualTo("HIGH");
            assertThat(result.getAssigneeName()).isEqualTo("Assignee");
            assertThat(result.getProjectName()).isEqualTo("Test Project");
        }

        @Test
        @DisplayName("존재하지 않는 태스크 조회 시 예외가 발생한다")
        void getTaskNotFound() {
            given(taskRepository.findByIdWithAssigneeAndProject(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTask(999L))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("태스크를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("프로젝트 소유자가 태스크를 생성한다")
        void createTaskSuccess() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("New Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .assigneeId(2L)
                    .projectId(1L)
                    .build();

            given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
            given(userRepository.findById(2L)).willReturn(Optional.of(assignee));
            given(taskRepository.save(any(Task.class))).willReturn(testTask);

            taskService.createTask(request, projectOwner.getId());

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(captor.capture());
            assertThat(captor.getValue().getTitle()).isEqualTo("New Task");
            assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
            assertThat(captor.getValue().getPriority()).isEqualTo(TaskPriority.MEDIUM);
        }

        @Test
        @DisplayName("프로젝트 소유자가 아닌 사용자가 태스크 생성 시 예외가 발생한다")
        void createTaskForbidden() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("New Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .assigneeId(2L)
                    .projectId(1L)
                    .build();

            given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));

            assertThatThrownBy(() -> taskService.createTask(request, otherUser.getId()))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 담당자로 태스크 생성 시 예외가 발생한다")
        void createTaskAssigneeNotFound() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("New Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .assigneeId(999L)
                    .projectId(1L)
                    .build();

            given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(request, projectOwner.getId()))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("담당자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트로 태스크 생성 시 예외가 발생한다")
        void createTaskProjectNotFound() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("New Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .assigneeId(2L)
                    .projectId(999L)
                    .build();

            given(projectRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(request, projectOwner.getId()))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTask {

        @Test
        @DisplayName("프로젝트 소유자가 태스크 제목을 수정한다")
        void updateTaskByOwner() {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated Title")
                    .build();

            given(taskRepository.findByIdWithAssigneeAndProject(1L)).willReturn(Optional.of(testTask));

            TaskResponse result = taskService.updateTask(1L, request, projectOwner.getId());

            assertThat(testTask.getTitle()).isEqualTo("Updated Title");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("담당자가 태스크 상태와 우선순위를 수정한다")
        void updateTaskByAssignee() {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.URGENT)
                    .build();

            given(taskRepository.findByIdWithAssigneeAndProject(1L)).willReturn(Optional.of(testTask));

            taskService.updateTask(1L, request, assignee.getId());

            assertThat(testTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(testTask.getPriority()).isEqualTo(TaskPriority.URGENT);
        }

        @Test
        @DisplayName("권한 없는 사용자가 태스크 수정 시 예외가 발생한다")
        void updateTaskForbidden() {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated")
                    .build();

            given(taskRepository.findByIdWithAssigneeAndProject(1L)).willReturn(Optional.of(testTask));

            assertThatThrownBy(() -> taskService.updateTask(1L, request, otherUser.getId()))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("권한이 없습니다");
        }

        @Test
        @DisplayName("태스크의 담당자를 변경한다")
        void updateTaskAssignee() {
            User newAssignee = User.builder()
                    .id(4L)
                    .email("new@test.com")
                    .name("New Assignee")
                    .password("encoded")
                    .role("MEMBER")
                    .build();

            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .assigneeId(4L)
                    .build();

            given(taskRepository.findByIdWithAssigneeAndProject(1L)).willReturn(Optional.of(testTask));
            given(userRepository.findById(4L)).willReturn(Optional.of(newAssignee));

            taskService.updateTask(1L, request, projectOwner.getId());

            assertThat(testTask.getAssignee().getId()).isEqualTo(4L);
        }

        @Test
        @DisplayName("존재하지 않는 태스크 수정 시 예외가 발생한다")
        void updateTaskNotFound() {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated")
                    .build();

            given(taskRepository.findByIdWithAssigneeAndProject(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(999L, request, projectOwner.getId()))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("태스크를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("프로젝트 소유자가 태스크를 삭제한다")
        void deleteTaskByOwner() {
            given(taskRepository.findByIdWithAssigneeAndProject(1L)).willReturn(Optional.of(testTask));

            taskService.deleteTask(1L, projectOwner.getId());

            verify(taskRepository).delete(testTask);
        }

        @Test
        @DisplayName("담당자가 태스크를 삭제한다")
        void deleteTaskByAssignee() {
            given(taskRepository.findByIdWithAssigneeAndProject(1L)).willReturn(Optional.of(testTask));

            taskService.deleteTask(1L, assignee.getId());

            verify(taskRepository).delete(testTask);
        }

        @Test
        @DisplayName("권한 없는 사용자가 태스크 삭제 시 예외가 발생한다")
        void deleteTaskForbidden() {
            given(taskRepository.findByIdWithAssigneeAndProject(1L)).willReturn(Optional.of(testTask));

            assertThatThrownBy(() -> taskService.deleteTask(1L, otherUser.getId()))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 태스크 삭제 시 예외가 발생한다")
        void deleteTaskNotFound() {
            given(taskRepository.findByIdWithAssigneeAndProject(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deleteTask(999L, projectOwner.getId()))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("태스크를 찾을 수 없습니다");
        }
    }
}
