import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-goal-progress',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './goal-progress.html',
  styleUrl: './goal-progress.css'
})
export class GoalProgress {
  @Input() percentage = 0;

  get clamped(): number {
    return Math.max(0, Math.min(this.percentage, 100));
  }
}
