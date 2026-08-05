/**
 * Sent to POST/PATCH /api/users/{userId}/tasks
 * Adjust field names here once the real TaskDTO is confirmed.
 */
export interface TaskRequest {
  userId: number;
  title: string;
  description: string;
  dueDate: string; // ISO date, e.g. "2026-08-15"
  completed?: boolean;
}

/**
 * Returned by the backend.
 */
export interface TaskResponse extends TaskRequest {
  id: number;
  completed: boolean;
}