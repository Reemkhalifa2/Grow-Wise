import { Component, Input } from '@angular/core';

export type StatusVariant = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  templateUrl: './status-badge.html',
  styleUrl: './status-badge.css'
})
export class StatusBadge {
  @Input() label = '';
  @Input() variant: StatusVariant = 'neutral';
}
