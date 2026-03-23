import { useEffect, useRef } from 'react';
import TaskForm from './TaskForm';
import { useCreateTask } from '../hooks/useTasks';
import type { CreateTaskRequest } from '../lib/types';

interface TaskCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function TaskCreateModal({
  isOpen,
  onClose,
  onSuccess,
}: TaskCreateModalProps) {
  const { createTask, isLoading, error } = useCreateTask();
  const overlayRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  async function handleSubmit(data: CreateTaskRequest) {
    try {
      await createTask(data);
      onSuccess();
      onClose();
    } catch {
      // error is handled by the hook
    }
  }

  function handleOverlayClick(e: React.MouseEvent) {
    if (e.target === overlayRef.current) onClose();
  }

  return (
    <div
      ref={overlayRef}
      role="dialog"
      aria-modal="true"
      aria-label="태스크 생성"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4"
      onClick={handleOverlayClick}
    >
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl border border-gray-200 animate-in fade-in">
        <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4">
          <h2 className="text-lg font-semibold text-gray-900">
            새 태스크 생성
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
            isCreate
            isSubmitting={isLoading}
            error={error}
            onSubmit={(data) => handleSubmit(data as CreateTaskRequest)}
            onCancel={onClose}
          />
        </div>
      </div>
    </div>
  );
}
