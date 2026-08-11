import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

/**
 * Circular progress ring. Used to show a goal's funding progress
 * (currentAmount / targetAmount) on plan cards — not an arbitrary
 * "plan completion" percentage.
 */
@Component({
  selector: 'app-plan-progress',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './plan-progress.html',
  styleUrl: './plan-progress.css'
})
export class PlanProgress {
  @Input() percentage = 0;
  @Input() size = 84;
  @Input() strokeWidth = 8;

  get clamped(): number {
    return Math.max(0, Math.min(this.percentage, 100));
  }

  get radius(): number {
    return (this.size - this.strokeWidth) / 2;
  }

  get circumference(): number {
    return 2 * Math.PI * this.radius;
  }

  get dashOffset(): number {
    return this.circumference * (1 - this.clamped / 100);
  }

  get center(): number {
    return this.size / 2;
  }
}
