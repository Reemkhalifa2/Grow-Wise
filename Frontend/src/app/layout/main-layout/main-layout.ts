import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, ElementRef, HostListener, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import { Auth } from '../../services/auth';
import { TaskService } from '../../services/task';
import { TaskResponse } from '../../models/task';

interface NavTarget {
  label: string;
  path: string;
  adminOnly?: boolean;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css'
})
export class MainLayout implements OnInit {

  private readonly authService = inject(Auth);
  private readonly taskService = inject(TaskService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly elementRef = inject(ElementRef);

  fullName = '';
  role = '';
  initials = '';
  currentDate = '';

  searchQuery = '';
  searchOpen = false;

  notificationsOpen = false;
  pendingTasks: TaskResponse[] = [];

  private readonly navTargets: NavTarget[] = [
    { label: 'Overview', path: '/dashboard' },
    { label: 'Portfolio', path: '/portfolio' },
    { label: 'Goals', path: '/financial-goal' },
    { label: 'Investment Plans', path: '/investment-plan' },
    { label: 'To Do', path: '/to-do' },
    { label: 'Financial Profile', path: '/financial-profile' },
    { label: 'Market Overview', path: '/market' },
    { label: 'Settings', path: '/settings' },
    { label: 'Help & Support', path: '/help' },
    { label: 'Admin Overview', path: '/admin-dashboard', adminOnly: true },
    { label: 'Assets', path: '/assets', adminOnly: true }
  ];

  ngOnInit(): void {
    // localStorage doesn't exist during server-side prerendering — only
    // read it once we're actually running in the browser.
    if (isPlatformBrowser(this.platformId)) {
      this.fullName =
        localStorage.getItem('fullName') ?? 'User';

      this.role =
        localStorage.getItem('role') ?? 'USER';

      this.initials = this.getInitials(
        this.fullName
      );

      this.loadPendingTasks();
    }

    this.currentDate =
      new Intl.DateTimeFormat('en-GB', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      }).format(new Date());
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get firstName(): string {
    return this.fullName.split(/\s+/)[0] || 'there';
  }

  get greeting(): string {
    const hour = new Date().getHours();

    if (hour < 12) {
      return `Good morning, ${this.firstName}`;
    }

    if (hour < 18) {
      return `Good afternoon, ${this.firstName}`;
    }

    return `Good evening, ${this.firstName}`;
  }

  get searchResults(): NavTarget[] {
    const query = this.searchQuery.trim().toLowerCase();

    if (!query) {
      return [];
    }

    return this.navTargets
      .filter(target => this.isAdmin || !target.adminOnly)
      .filter(target => target.label.toLowerCase().includes(query))
      .slice(0, 6);
  }

  goTo(target: NavTarget): void {
    this.searchQuery = '';
    this.searchOpen = false;
    this.router.navigate([target.path]);
  }

  toggleNotifications(): void {
    this.notificationsOpen = !this.notificationsOpen;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.searchOpen = false;
      this.notificationsOpen = false;
    }
  }

  private loadPendingTasks(): void {
    const userId = this.authService.getUserId();

    if (userId === null) {
      return;
    }

    this.taskService.listByUser(userId).subscribe({
      next: tasks => {
        this.pendingTasks = (tasks ?? [])
          .filter(task => !task.completed)
          .slice(0, 5);
      },
      error: () => {
        // Notifications are a convenience, not critical — fail quietly.
        this.pendingTasks = [];
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private getInitials(name: string): string {
    return name
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map(part => part.charAt(0).toUpperCase())
      .join('');
  }
}
