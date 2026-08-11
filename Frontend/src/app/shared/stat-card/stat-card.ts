import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.css'
})
export class StatCard {
  @Input() label = '';
  @Input() value = '';
  @Input() footnote = '';

  /** null = neutral footnote color, true = green, false = red */
  @Input() footnoteTone: boolean | null = null;
}
