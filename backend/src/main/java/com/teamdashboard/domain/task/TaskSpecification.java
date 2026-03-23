package com.teamdashboard.domain.task;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> withFilters(TaskStatus status, TaskPriority priority,
                                                   Long assigneeId, Long projectId) {
        return Specification.where(hasStatus(status))
                .and(hasPriority(priority))
                .and(hasAssignee(assigneeId))
                .and(hasProject(projectId))
                .and(fetchAssociations());
    }

    private static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> {
            if (priority == null) return null;
            return cb.equal(root.get("priority"), priority);
        };
    }

    private static Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) return null;
            return cb.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    private static Specification<Task> hasProject(Long projectId) {
        return (root, query, cb) -> {
            if (projectId == null) return null;
            return cb.equal(root.get("project").get("id"), projectId);
        };
    }

    private static Specification<Task> fetchAssociations() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("assignee", JoinType.INNER);
                root.fetch("project", JoinType.INNER);
            }
            return null;
        };
    }
}
