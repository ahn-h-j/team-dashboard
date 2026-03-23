export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Task {
  id: number;
  title: string;
  status: TaskStatus;
  priority: TaskPriority;
  assigneeId: number;
  assigneeName: string;
  projectId: number;
  projectName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  title: string;
  status: TaskStatus;
  priority: TaskPriority;
  assigneeId: number;
  projectId: number;
}

export interface UpdateTaskRequest {
  title?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  assigneeId?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
