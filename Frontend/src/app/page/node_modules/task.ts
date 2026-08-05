import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { TaskRequest, TaskResponse } from '../models/task';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  create(userId: number, task: TaskRequest): Observable<TaskResponse> {
    return this.http.post<TaskResponse>(`${this.apiUrl}/${userId}/tasks`, task);
  }

  listByUser(userId: number): Observable<TaskResponse[]> {
    return this.http.get<TaskResponse[]>(`${this.apiUrl}/${userId}/tasks`);
  }

  update(userId: number, taskId: number, task: Partial<TaskRequest>): Observable<TaskResponse> {
    return this.http.patch<TaskResponse>(`${this.apiUrl}/${userId}/tasks/${taskId}`, task);
  }

  markComplete(userId: number, taskId: number, completed: boolean): Observable<TaskResponse> {
    return this.http.put<TaskResponse>(
      `${this.apiUrl}/${userId}/tasks/${taskId}/complete?completed=${completed}`,
      {}
    );
  }

  delete(userId: number, taskId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${userId}/tasks/${taskId}`, {
      responseType: 'text'
    });
  }
}