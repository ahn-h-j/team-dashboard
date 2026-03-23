import { useEffect, useRef, useState } from 'react';
import TaskForm from './TaskForm';
import { useUpdateTask, useDeleteTask } from '../hooks/useTasks';
import type { Task, UpdateTaskRequest } from '../lib/types';

interface TaskEditModalProps {
  task: Task | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function TaskEditModal({
  task,
  isOpen,
  onClose,
  onSuccess,
}: TaskEditModalProps) {
  const { updateTask, isLoading: isUpdating, error: updateError } =
    useUpdateTask();
  const { deleteTask, isLoading: isDeleting, error: deleteError } =
    useDeleteTask();
  const [confirmDelete, setConfirmDelete] = useState(false);
  const overlayRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;
    setConfirmDelete(false);
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen || !task) return null;

  async function handleSubmit(data: UpdateTaskRequest) {
    if (!task) return;
    try {
      await updateTask(task.id, data);
      onSuccess();
      onClose();
    } catch {
      // error handled by hook
    }
  }

  async function handleDelete() {
    if (!task) return;
    if (!confirmDelete) {
      setConfirmDelete(true);
      return;
    }
    try {
      await deleteTask(task.id);
      onSuccess();
      onClose();
    } catch {
      // error handled by hook
    }
  }

  function handleOverlayClick(e: React.MouseEvent) {
    if (e.target === overlayRef.current) onClose();
  }

  const error = updateError || deleteError;
  const isBusy = isUpdating || isDeleting;

  return (
    <div
      ref={overlayRef}
      role="dialog"
      aria-modal="true"
      aria-label="태스크 수정"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4"
      onClick={handleOverlayClick}
    >
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl border border-gray-200">
        <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4">
          <h2 className="text-lg font-semibold text-gray-900">
            태스크 수정
          </h2>
          <button
            onClick={onClose}
            aria-label="닫기"
            className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
          >
            <svg
              className="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1.5}
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
        <div className="px-6 py-5">
          <TaskForm
            initialData={task}
            isCreate={false}
            isSubmitting={isBusy}
            error={error}
            onSubmit={(data) => handleSubmit(data as UpdateTaskRequest)}
            onCancel={onClose}
          />

          {/* 삭제 영역 */}
          <div className="mt-6 border-t border-gray-200 pt-4">
            <div className="flex items-center justify-between">
              <p className="text-sm text-gray-500">
                {confirmDelete
                  ? '정말 삭제하시겠습니까?'
                  : '이 태스크를 삭제할 수 있습니다.'}
              </p>
              <div className="flex gap-2">
                {confirmDelete && (
                  <button
                    type="button"
                    onClick={() => setConfirmDelete(false)}
                    disabled={isDeleting}
                    className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-50"
                  >
                    취소
                  </button>
                )}
                <button
                  type="button"
                  onClick={handleDelete}
                  disabled={isBusy}
                  className={`inline-flex items-center rounded-lg px-3 py-1.5 text-sm font-medium text-white transition-colors disabled:opacity-50 ${
                    confirmDelete
                      ? 'bg-red-600 hover:bg-red-700'
                      : 'bg-red-500 hover:bg-red-600'
                  }`}
                >
                  {isDeleting && (
                    <span className="mr-1.5 h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent" />
                  )}
                  {confirmDelete ? '삭제 확인' : '삭제'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
