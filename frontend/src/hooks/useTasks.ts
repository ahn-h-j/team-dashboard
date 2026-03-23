import { useState, useEffect, useCallback } from 'react';
import { api } from '../lib/api';
import type {
  Task,
  TaskStatus,
  TaskPriority,
  CreateTaskRequest,
  UpdateTaskRequest,
  PageResponse,
} from '../lib/types';

export interface TaskFilters {
  status?: TaskStatus;
  priority?: TaskPriority;
  assigneeId?: number;
  projectId?: number;
  page?: number;
  size?: number;
  sort?: string;
}

function buildQuery(filters?: TaskFilters): string {
  if (!filters) return '';
  const params = new URLSearchParams();
  if (filters.status) params.set('status', filters.status);
  if (filters.priority) params.set('priority', filters.priority);
  if (filters.assigneeId != null)
    params.set('assigneeId', String(filters.assigneeId));
  if (filters.projectId != null)
    params.set('projectId', String(filters.projectId));
  if (filters.page != null) params.set('page', String(filters.page));
  if (filters.size != null) params.set('size', String(filters.size));
  if (filters.sort) params.set('sort', filters.sort);
  const qs = params.toString();
  return qs ? `?${qs}` : '';
}

export function useTasks(filters?: TaskFilters) {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const filtersKey = JSON.stringify(filters);

  const fetch = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const parsed: TaskFilters | undefined = filtersKey
        ? JSON.parse(filtersKey)
        : undefined;
      const data = await api.get<PageResponse<Task>>(
        `/api/tasks${buildQuery(parsed)}`,
      );
      setTasks(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch tasks');
    } finally {
      setIsLoading(false);
    }
  }, [filtersKey]);

  useEffect(() => {
    void fetch();
  }, [fetch]);

  return { tasks, totalPages, isLoading, error, refetch: fetch };
}

export function useTask(id: number | null) {
  const [task, setTask] = useState<Task | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id == null) return;
    setIsLoading(true);
    setError(null);
    api
      .get<Task>(`/api/tasks/${id}`)
      .then(setTask)
      .catch((err: unknown) => {
        setError(
          err instanceof Error ? err.message : 'Failed to fetch task',
        );
      })
      .finally(() => setIsLoading(false));
  }, [id]);

  return { task, isLoading, error };
}

export function useCreateTask() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createTask = useCallback(async (data: CreateTaskRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await api.post<Task>('/api/tasks', data);
      return result;
    } catch (err) {
      const message =
        err instanceof Error ? err.message : 'Failed to create task';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  return { createTask, isLoading, error };
}

export function useUpdateTask() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateTask = useCallback(
    async (id: number, data: UpdateTaskRequest) => {
      setIsLoading(true);
      setError(null);
      try {
        const result = await api.patch<Task>(`/api/tasks/${id}`, data);
        return result;
      } catch (err) {
        const message =
          err instanceof Error ? err.message : 'Failed to update task';
        setError(message);
        throw err;
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  return { updateTask, isLoading, error };
}

export function useDeleteTask() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const deleteTask = useCallback(async (id: number) => {
    setIsLoading(true);
    setError(null);
    try {
      await api.delete<void>(`/api/tasks/${id}`);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : 'Failed to delete task';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  return { deleteTask, isLoading, error };
}
