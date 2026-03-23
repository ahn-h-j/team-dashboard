import { type FormEvent, useState, useEffect } from 'react';
import type {
  TaskStatus,
  TaskPriority,
  CreateTaskRequest,
  UpdateTaskRequest,
  Task,
} from '../lib/types';

const STATUS_OPTIONS: { value: TaskStatus; label: string }[] = [
  { value: 'TODO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'IN_REVIEW', label: 'In Review' },
  { value: 'DONE', label: 'Done' },
];

const PRIORITY_OPTIONS: { value: TaskPriority; label: string }[] = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'URGENT', label: 'Urgent' },
];

interface TaskFormProps {
  /** Pre-fill with existing task data for editing */
  initialData?: Task;
  /** True when creating, false when editing */
  isCreate: boolean;
  isSubmitting: boolean;
  error: string | null;
  onSubmit: (data: CreateTaskRequest | UpdateTaskRequest) => void;
  onCancel: () => void;
}

export default function TaskForm({
  initialData,
  isCreate,
  isSubmitting,
  error,
  onSubmit,
  onCancel,
}: TaskFormProps) {
  const [title, setTitle] = useState(initialData?.title ?? '');
  const [status, setStatus] = useState<TaskStatus>(
    initialData?.status ?? 'TODO',
  );
  const [priority, setPriority] = useState<TaskPriority>(
    initialData?.priority ?? 'MEDIUM',
  );
  const [assigneeId, setAssigneeId] = useState<string>(
    initialData?.assigneeId != null ? String(initialData.assigneeId) : '',
  );
  const [projectId, setProjectId] = useState<string>(
    initialData?.projectId != null ? String(initialData.projectId) : '',
  );
  const [validationError, setValidationError] = useState('');

  useEffect(() => {
    if (initialData) {
      setTitle(initialData.title);
      setStatus(initialData.status);
      setPriority(initialData.priority);
      setAssigneeId(String(initialData.assigneeId));
      setProjectId(String(initialData.projectId));
    }
  }, [initialData]);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setValidationError('');

    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      setValidationError('제목을 입력해주세요.');
      return;
    }
    if (trimmedTitle.length > 200) {
      setValidationError('제목은 200자 이내로 입력해주세요.');
      return;
    }

    if (isCreate) {
      const parsedAssigneeId = Number(assigneeId);
      const parsedProjectId = Number(projectId);
      if (!assigneeId || isNaN(parsedAssigneeId)) {
        setValidationError('담당자 ID를 입력해주세요.');
        return;
      }
      if (!projectId || isNaN(parsedProjectId)) {
        setValidationError('프로젝트 ID를 입력해주세요.');
        return;
      }
      const payload: CreateTaskRequest = {
        title: trimmedTitle,
        status,
        priority,
        assigneeId: parsedAssigneeId,
        projectId: parsedProjectId,
      };
      onSubmit(payload);
    } else {
      const payload: UpdateTaskRequest = {
        title: trimmedTitle,
        status,
        priority,
      };
      if (assigneeId) {
        const parsed = Number(assigneeId);
        if (!isNaN(parsed)) payload.assigneeId = parsed;
      }
      onSubmit(payload);
    }
  }

  const displayError = validationError || error;

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {displayError && (
        <div
          role="alert"
          className="rounded-lg bg-red-50 p-3 text-sm text-red-700 border border-red-200"
        >
          {displayError}
        </div>
      )}

      {/* 제목 */}
      <div>
        <label
          htmlFor="task-title"
          className="block text-sm font-medium text-gray-700 mb-1"
        >
          제목 <span className="text-red-500">*</span>
        </label>
        <input
          id="task-title"
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          maxLength={200}
          placeholder="태스크 제목을 입력하세요"
          className="block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          autoFocus
        />
        <p className="mt-1 text-xs text-gray-400">{title.length}/200</p>
      </div>

      {/* 상태 */}
      <div>
        <label
          htmlFor="task-status"
          className="block text-sm font-medium text-gray-700 mb-1"
        >
          상태
        </label>
        <select
          id="task-status"
          value={status}
          onChange={(e) => setStatus(e.target.value as TaskStatus)}
          className="block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
        >
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>

      {/* 우선순위 */}
      <div>
        <label
          htmlFor="task-priority"
          className="block text-sm font-medium text-gray-700 mb-1"
        >
          우선순위
        </label>
        <select
          id="task-priority"
          value={priority}
          onChange={(e) => setPriority(e.target.value as TaskPriority)}
          className="block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
        >
          {PRIORITY_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>

      {/* 담당자 ID */}
      <div>
        <label
          htmlFor="task-assignee"
          className="block text-sm font-medium text-gray-700 mb-1"
        >
          담당자 ID {isCreate && <span className="text-red-500">*</span>}
        </label>
        <input
          id="task-assignee"
          type="number"
          value={assigneeId}
          onChange={(e) => setAssigneeId(e.target.value)}
          placeholder="담당자 ID"
          min={1}
          className="block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
        />
      </div>

      {/* 프로젝트 ID (only on create) */}
      {isCreate && (
        <div>
          <label
            htmlFor="task-project"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            프로젝트 ID <span className="text-red-500">*</span>
          </label>
          <input
            id="task-project"
            type="number"
            value={projectId}
            onChange={(e) => setProjectId(e.target.value)}
            placeholder="프로젝트 ID"
            min={1}
            className="block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </div>
      )}

      {/* 버튼 */}
      <div className="flex items-center justify-end gap-3 pt-2">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
          className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors disabled:opacity-50"
        >
          취소
        </button>
        <button
          type="submit"
          disabled={isSubmitting}
          className="inline-flex items-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors disabled:opacity-50"
        >
          {isSubmitting && (
            <span className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
          )}
          {isCreate ? '생성' : '저장'}
        </button>
      </div>
    </form>
  );
}
