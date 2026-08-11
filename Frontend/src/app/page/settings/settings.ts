import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, OnInit, PLATFORM_ID, inject } from '@angular/core';

import { PageHeader } from '../../shared/page-header/page-header';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, PageHeader],
  templateUrl: './settings.html',
  styleUrl: './settings.css'
})
export class Settings implements OnInit {

  private readonly platformId = inject(PLATFORM_ID);

  fullName = '';
  email = '';
  role = '';

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.fullName = localStorage.getItem('fullName') ?? 'User';
    this.email = localStorage.getItem('email') ?? '—';
    this.role = localStorage.getItem('role') ?? 'USER';
  }
}
