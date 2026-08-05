import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { TaskService } from '../../services/task';
import { TaskRequest, TaskResponse } from '../../models/task';

@Component({
  selector: 'app-task',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './task.component.html',
  styleUrls: ['./task.component.css']
})
export class TaskComponent implements OnInit {

  private readonly taskService = inject(TaskService);

  tasks: TaskResponse[] = [];
  loading = true;

  showModal = false;
  editingId: number | null = null;

  newTask = {
    title: '',
    description: '',
    dueDate: '',
    completed: false
  };

  /**
   * Tries several common places apps store the logged-in user's id, so this
   * works regardless of exactly how the login page saved it. Checks both
   * localStorage and sessionStorage, under several common key names.
   */
  private get userId(): number {
    const stores = [localStorage, sessionStorage];

    // 1) A direct numeric id under a common key name.
    const directKeys = ['userId', 'id', 'user_id'];
    for (const store of stores) {
      for (const key of directKeys) {
        const value = Number(store.getItem(key));
        if (value > 0) {
          return value;
        }
      }
    }

    // 2) A user/auth object (JSON) with an .id / .userId field.
    const objectKeys = ['user', 'currentUser', 'authUser', 'loggedInUser'];
    for (const store of stores) {
      for (const key of objectKeys) {
        const raw = store.getItem(key);
        if (!raw) {
          continue;
        }

        try {
          const parsed = JSON.parse(raw);
          const id = Number(parsed?.id ?? parsed?.userId);
          if (id > 0) {
            return id;
          }
        } catch {
          // not JSON, ignore
        }
      }
    }

    // 3) Decode a JWT stored under a common token key and read its claims.
    const tokenKeys = ['token', 'authToken', 'accessToken', 'jwt'];
    for (const store of stores) {
      for (const key of tokenKeys) {
        const token = store.getItem(key);
        if (!token || !token.includes('.')) {
          continue;
        }

        try {
          const payload = token.split('.')[1];
          const decoded = JSON.parse(atob(payload));
          const id = Number(decoded?.id ?? decoded?.userId ?? decoded?.sub);
          if (id > 0) {
            return id;
          }
        } catch {
          // not a valid JWT, ignore
        }
      }
    }

    console.warn(
      'Could not determine the logged-in user id. ' +
      'Run `JSON.stringify(localStorage)` and `JSON.stringify(sessionStorage)` in the console ' +
      'to see what is actually stored after login.'
    );
    return 0;
  }

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.loading = true;

    this.taskService
      .listByUser(this.userId)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: tasks => {
          this.tasks = tasks;
        },

        error: error => {
          console.error('Failed to load tasks:', error);
        }
      });
  }

  openModal(): void {
    this.editingId = null;
    this.newTask = { title: '', description: '', dueDate: '', completed: false };
    this.showModal = true;
  }

  editTask(task: TaskResponse): void {
    this.editingId = task.id;
    this.newTask = {
      title: task.title,
      description: task.description,
      dueDate: task.dueDate,
      completed: task.completed
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingId = null;
    this.newTask = { title: '', description: '', dueDate: '', completed: false };
  }

  saveTask(): void {
    if (this.editingId) {
      this.taskService
        .update(this.userId, this.editingId, this.newTask)
        .subscribe({
          next: () => {
            this.loadTasks();
            this.closeModal();
          },
          error: error => console.error('Failed to update task:', error)
        });

      return;
    }

    const request: TaskRequest = {
      userId: this.userId,
      title: this.newTask.title,
      description: this.newTask.description,
      dueDate: this.newTask.dueDate,
      completed: false
    };

    this.taskService
      .create(this.userId, request)
      .subscribe({
        next: () => {
          this.loadTasks();
          this.closeModal();
        },
        error: error => console.error('Failed to create task:', error)
      });
  }

  deleteTask(task: TaskResponse): void {
    const confirmed = window.confirm(`Delete "${task.title}"?`);

    if (!confirmed) {
      return;
    }

    this.taskService
      .delete(this.userId, task.id)
      .subscribe({
        next: () => this.loadTasks(),
        error: error => console.error('Failed to delete task:', error)
      });
  }

  toggleStatus(task: TaskResponse): void {
    this.taskService
      .markComplete(this.userId, task.id, !task.completed)
      .subscribe({
        next: () => this.loadTasks(),
        error: error => console.error('Failed to update task status:', error)
      });
  }
}