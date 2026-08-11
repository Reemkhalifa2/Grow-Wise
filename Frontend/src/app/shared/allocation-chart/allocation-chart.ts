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
  ArcElement,
  Chart,
  DoughnutController,
  Legend,
  Tooltip
} from 'chart.js';

Chart.register(DoughnutController, ArcElement, Tooltip, Legend);

export interface AllocationSlice {
  label: string;
  value: number;
}

/**
 * Small self-contained doughnut chart for showing how a plan or portfolio
 * is split across assets. Each instance owns its own canvas + Chart.js
 * instance so several can be rendered side by side (e.g. one per plan
 * card) without fighting over shared state.
 */
@Component({
  selector: 'app-allocation-chart',
  standalone: true,
  templateUrl: './allocation-chart.html',
  styleUrl: './allocation-chart.css'
})
export class AllocationChart implements AfterViewInit, OnChanges, OnDestroy {

  @Input() slices: AllocationSlice[] = [];
  @Input() centerLabel = '';
  @Input() centerValue = '';

  @ViewChild('canvas') private readonly canvasRef!: ElementRef<HTMLCanvasElement>;

  private chart: Chart | null = null;

  private static readonly PALETTE = [
    '#146a4f',
    '#6366f1',
    '#d4a017',
    '#0ea5e9',
    '#dc2626',
    '#0e4d39',
    '#8b5cf6',
    '#64748b'
  ];

  ngAfterViewInit(): void {
    this.render();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['slices'] && this.canvasRef) {
      this.render();
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private render(): void {
    const context = this.canvasRef?.nativeElement?.getContext('2d');

    if (!context) {
      return;
    }

    const labels = this.slices.map(slice => slice.label);
    const values = this.slices.map(slice => slice.value);
    const colors = this.slices.map(
      (_, index) => AllocationChart.PALETTE[index % AllocationChart.PALETTE.length]
    );

    if (this.chart) {
      this.chart.data.labels = labels;
      this.chart.data.datasets[0].data = values;
      this.chart.data.datasets[0].backgroundColor = colors;
      this.chart.update();
      return;
    }

    this.chart = new Chart(context, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [
          {
            data: values,
            backgroundColor: colors,
            borderColor: '#ffffff',
            borderWidth: 2,
            hoverOffset: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '72%',
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (tooltipItem) => {
                const value = Number(tooltipItem.raw ?? 0);
                return ` ${tooltipItem.label}: ${value.toFixed(3)} OMR`;
              }
            }
          }
        }
      }
    });
  }
}
