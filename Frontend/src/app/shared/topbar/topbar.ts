import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-topbar',
  standalone: true,
  templateUrl: './topbar.html',
  styleUrl: './topbar.css'
})
export class Topbar implements OnInit {

  fullName = '';
  role = '';
  initials = '';
  currentDate = '';

  ngOnInit(): void {
    this.fullName =
      localStorage.getItem('fullName') ?? 'User';

    this.role =
      localStorage.getItem('role') ?? 'USER';

    this.initials =
      this.getInitials(this.fullName);

    this.currentDate =
      new Intl.DateTimeFormat('en-GB', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      }).format(new Date());
  }

  private getInitials(fullName: string): string {
    return fullName
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map(part => part.charAt(0).toUpperCase())
      .join('');
  }
}