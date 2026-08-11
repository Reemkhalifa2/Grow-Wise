import { Component, Input } from '@angular/core';

export type GoalIconKey = 'car' | 'home' | 'travel' | 'education' | 'savings' | 'target';

@Component({
  selector: 'app-goal-icon',
  standalone: true,
  templateUrl: './goal-icon.html',
  styleUrl: './goal-icon.css'
})
export class GoalIcon {
  @Input() icon: GoalIconKey = 'target';
}
