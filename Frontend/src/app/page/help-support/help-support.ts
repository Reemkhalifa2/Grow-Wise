import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

import { PageHeader } from '../../shared/page-header/page-header';

interface FaqItem {
  question: string;
  answer: string;
}

@Component({
  selector: 'app-help-support',
  standalone: true,
  imports: [CommonModule, PageHeader],
  templateUrl: './help-support.html',
  styleUrl: './help-support.css'
})
export class HelpSupport {
  readonly faqs: FaqItem[] = [
    {
      question: 'How is my portfolio value calculated?',
      answer: 'Each investment tracks the units you purchased and the price at purchase time. Current value is units × the asset’s latest price, so it reflects real market movement rather than your original contribution.'
    },
    {
      question: 'What happens when I complete a monthly contribution?',
      answer: 'GlowWise records the contribution against the plan’s linked asset allocation and prevents a second contribution from being counted toward the same calendar month.'
    },
    {
      question: 'How is a goal’s status determined?',
      answer: 'A goal is On Track, Needs Attention, or Behind based on comparing the contribution pace required to reach your target date against your plan’s current monthly investment amount.'
    },
    {
      question: 'Can I change my monthly investment amount?',
      answer: 'Yes — update it from the Investment Plans page. Changes apply to future contributions only.'
    }
  ];
}
