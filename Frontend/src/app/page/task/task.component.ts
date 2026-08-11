import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
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
  private readonly cdr = inject(ChangeDetectorRef); // 1. Inject ChangeDetectorRef

  tasks: TaskResponse[] = [];
  loading = true;
  searchQuery = '';

  showModal = false;
  editingId: number | null = null;

  newTask = {
    title: '',
    description: '',
    dueDate: '',
    completed: false
  };

  private get userId(): number {
    const stores = [localStorage, sessionStorage];

    // 1) Direct numeric id check
    const directKeys = ['userId', 'id', 'user_id'];
    for (const store of stores) {
      for (const key of directKeys) {
        const value = Number(store.getItem(key));
        if (value > 0) return value;
      }
    }

    // 2) User object check
    const objectKeys = ['user', 'currentUser', 'authUser', 'loggedInUser'];
    for (const store of stores) {
      for (const key of objectKeys) {
        const raw = store.getItem(key);
        if (!raw) continue;
        try {
          const parsed = JSON.parse(raw);
          const id = Number(parsed?.id ?? parsed?.userId);
          if (id > 0) return id;
        } catch {
          // not JSON, ignore
        }
      }
    }

    // 3) JWT check
    const tokenKeys = ['token', 'authToken', 'accessToken', 'jwt'];
    for (const store of stores) {
      for (const key of tokenKeys) {
        const token = store.getItem(key);
        if (!token || !token.includes('.')) continue;
        try {
          const payload = token.split('.')[1];
          const decoded = JSON.parse(atob(payload));
          const id = Number(decoded?.id ?? decoded?.userId ?? decoded?.sub);
          if (id > 0) return id;
        } catch {
          // not a valid JWT, ignore
        }
      }
    }

    console.warn('Could not determine logged-in user id.');
    return 0;
  }

  ngOnInit(): void {
    this.loadTasks();
  }

  get filteredTasks(): TaskResponse[] {
    const query = this.searchQuery.trim().toLowerCase();

    if (!query) {
      return this.tasks;
    }

    return this.tasks.filter(task =>
      task.title?.toLowerCase().includes(query) ||
      task.description?.toLowerCase().includes(query)
    );
  }

  get pendingCount(): number {
    return this.tasks.filter(task => !task.completed).length;
  }

  isOverdue(task: TaskResponse): boolean {
    if (!task.dueDate || task.completed) {
      return false;
    }

    return new Date(task.dueDate) < new Date(new Date().toDateString());
  }

  loadTasks(): void {
    this.loading = true;

    this.taskService
      .listByUser(this.userId)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges(); // 2. Trigger detection when loading completes
        })
      )
      .subscribe({
        next: tasks => {
          this.tasks = tasks;
          this.cdr.detectChanges(); // 3. Ensure list updates on screen instantly
        },
        error: error => {
          console.error('Failed to load tasks:', error);
          this.cdr.detectChanges();
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

    if (!confirmed) return;

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