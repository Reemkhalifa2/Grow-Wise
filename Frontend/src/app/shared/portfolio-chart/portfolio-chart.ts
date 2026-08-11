import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {
  CategoryScale,
  Chart,
  Filler,
  LineController,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip
} from 'chart.js';

Chart.register(
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Filler,
  Tooltip
);

export type ChartPeriod = '1M' | '3M' | '6M' | '1Y' | 'ALL';

export interface ContributionPoint {
  /** ISO date string */
  date: string;
  amountInvested: number;
}

/**
 * Cumulative contribution activity over time, with period filtering.
 * This is deliberately NOT a "portfolio value over time" chart — the
 * backend has no historical price snapshots, so plotting a fabricated
 * value history would misrepresent real performance. What IS real and
 * derivable is how much the user has actually contributed over time.
 */
@Component({
  selector: 'app-portfolio-chart',
  standalone: true,
  templateUrl: './portfolio-chart.html',
  styleUrl: './portfolio-chart.css'
})
export class PortfolioChart implements AfterViewInit, OnChanges, OnDestroy {

  @Input() points: ContributionPoint[] = [];

  @ViewChild('canvas') private readonly canvasRef!: ElementRef<HTMLCanvasElement>;

  period: ChartPeriod = 'ALL';
  readonly periods: ChartPeriod[] = ['1M', '3M', '6M', '1Y', 'ALL'];

  private chart: Chart | null = null;

  ngAfterViewInit(): void {
    this.render();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['points'] && this.canvasRef) {
      this.render();
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  selectPeriod(period: ChartPeriod): void {
    this.period = period;
    this.render();
  }

  get hasData(): boolean {
    return this.filteredSeries().length > 0;
  }

  private filteredSeries(): { label: string; total: number }[] {
    const sorted = [...this.points]
      .filter(point => !!point.date)
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());

    const cutoff = this.cutoffDate();
    const inRange = cutoff
      ? sorted.filter(point => new Date(point.date) >= cutoff)
      : sorted;

    let running = 0;

    // Contributions before the visible window still count toward the
    // running total shown at the window's start, so the line reflects
    // real cumulative investment rather than resetting to zero.
    if (cutoff) {
      running = sorted
        .filter(point => new Date(point.date) < cutoff)
        .reduce((sum, point) => sum + Number(point.amountInvested || 0), 0);
    }

    return inRange.map(point => {
      running += Number(point.amountInvested || 0);

      return {
        label: new Date(point.date).toLocaleDateString('en-GB', {
          day: '2-digit',
          month: 'short'
        }),
        total: running
      };
    });
  }

  private cutoffDate(): Date | null {
    if (this.period === 'ALL') {
      return null;
    }

    const now = new Date();
    const cutoff = new Date(now);

    switch (this.period) {
      case '1M':
        cutoff.setMonth(now.getMonth() - 1);
        break;
      case '3M':
        cutoff.setMonth(now.getMonth() - 3);
        break;
      case '6M':
        cutoff.setMonth(now.getMonth() - 6);
        break;
      case '1Y':
        cutoff.setFullYear(now.getFullYear() - 1);
        break;
    }

    return cutoff;
  }

  private render(): void {
    const context = this.canvasRef?.nativeElement?.getContext('2d');

    if (!context) {
      return;
    }

    const series = this.filteredSeries();
    const labels = series.map(point => point.label);
    const data = series.map(point => point.total);

    if (this.chart) {
      this.chart.data.labels = labels;
      this.chart.data.datasets[0].data = data;
      this.chart.update();
      return;
    }

    const gradient = context.createLinearGradient(0, 0, 0, 220);
    gradient.addColorStop(0, 'rgba(20, 106, 79, 0.25)');
    gradient.addColorStop(1, 'rgba(20, 106, 79, 0)');

    this.chart = new Chart(context, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            data,
            borderColor: '#146a4f',
            backgroundColor: gradient,
            fill: true,
            tension: 0.35,
            pointRadius: 0,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: '#146a4f',
            borderWidth: 2.5
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        scales: {
          x: {
            grid: { display: false },
            ticks: { color: '#94a3b8', font: { size: 11 } }
          },
          y: {
            grid: { color: '#edf1f9' },
            ticks: {
              color: '#94a3b8',
              font: { size: 11 },
              callback: value => `${value}`
            }
          }
        },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#0a3a2b',
            padding: 10,
            cornerRadius: 8,
            callbacks: {
              label: tooltipItem => ` OMR ${Number(tooltipItem.raw).toFixed(3)} invested`
            }
          }
        }
      }
    });
  }
}
