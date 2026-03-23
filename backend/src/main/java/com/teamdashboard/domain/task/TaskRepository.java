package com.teamdashboard.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t JOIN FETCH t.assignee JOIN FETCH t.project WHERE t.id = :id")
    Optional<Task> findByIdWithAssigneeAndProject(Long id);
}
