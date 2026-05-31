import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { Dashboard } from '../../models/expense.model';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, AfterViewInit {
  @ViewChild('pieChart') pieChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('barChart') barChartRef!: ElementRef<HTMLCanvasElement>;

  dashboard: Dashboard | null = null;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  loading = false;
  error: string | null = null;

  private pieChart: Chart | null = null;
  private barChart: Chart | null = null;

  months = [
    { value: 1, key: 'MONTHS.JANUARY' },
    { value: 2, key: 'MONTHS.FEBRUARY' },
    { value: 3, key: 'MONTHS.MARCH' },
    { value: 4, key: 'MONTHS.APRIL' },
    { value: 5, key: 'MONTHS.MAY' },
    { value: 6, key: 'MONTHS.JUNE' },
    { value: 7, key: 'MONTHS.JULY' },
    { value: 8, key: 'MONTHS.AUGUST' },
    { value: 9, key: 'MONTHS.SEPTEMBER' },
    { value: 10, key: 'MONTHS.OCTOBER' },
    { value: 11, key: 'MONTHS.NOVEMBER' },
    { value: 12, key: 'MONTHS.DECEMBER' }
  ];

  years: number[] = [];

  constructor(private apiService: ApiService, private translate: TranslateService) {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear; i >= 2018; i--) {
      this.years.push(i);
    }
  }

  ngOnInit(): void {
    this.loadDashboard();
  }

  ngAfterViewInit(): void {
    // Charts will be initialized after data is loaded
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;

    this.apiService.getDashboard(this.selectedYear, this.selectedMonth).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
        setTimeout(() => this.updateCharts(), 100);
      },
      error: (err) => {
        this.error = 'Nie udało się załadować danych. Sprawdź połączenie z serwerem.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onMonthChange(): void {
    this.loadDashboard();
  }

  private updateCharts(): void {
    if (!this.dashboard) return;

    this.updatePieChart();
    this.updateBarChart();
  }

  private updatePieChart(): void {
    if (!this.pieChartRef?.nativeElement || !this.dashboard?.expensesByCategory) return;

    if (this.pieChart) {
      this.pieChart.destroy();
    }

    const ctx = this.pieChartRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const data = this.dashboard.expensesByCategory;
    const colors = this.generateColors(data.length);

    this.pieChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: data.map(d => d.categoryName),
        datasets: [{
          data: data.map(d => d.amount),
          backgroundColor: colors,
          borderWidth: 2,
          borderColor: '#fff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 20,
              font: { size: 12 }
            }
          },
          title: {
            display: true,
            text: this.translate.instant('DASHBOARD.EXPENSES_BY_CATEGORY'),
            font: { size: 16, weight: 'bold' }
          }
        }
      }
    });
  }

  private updateBarChart(): void {
    if (!this.barChartRef?.nativeElement || !this.dashboard?.monthlyTrends) return;

    if (this.barChart) {
      this.barChart.destroy();
    }

    const ctx = this.barChartRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const data = this.dashboard.monthlyTrends.slice(-12);

    this.barChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: data.map(d => d.monthName),
        datasets: [
          {
            label: this.translate.instant('DASHBOARD.INCOME'),
            data: data.map(d => d.income),
            backgroundColor: 'rgba(34, 197, 94, 0.7)',
            borderColor: 'rgb(34, 197, 94)',
            borderWidth: 1
          },
          {
            label: this.translate.instant('DASHBOARD.EXPENSES'),
            data: data.map(d => d.expenses),
            backgroundColor: 'rgba(239, 68, 68, 0.7)',
            borderColor: 'rgb(239, 68, 68)',
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top'
          },
          title: {
            display: true,
            text: this.translate.instant('DASHBOARD.INCOME_VS_EXPENSES_CHART'),
            font: { size: 16, weight: 'bold' }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) => value.toLocaleString('pl-PL') + ' zł'
            }
          }
        }
      }
    });
  }

  private generateColors(count: number): string[] {
    const baseColors = [
      '#3B82F6', '#EF4444', '#10B981', '#F59E0B', '#8B5CF6',
      '#EC4899', '#06B6D4', '#84CC16', '#F97316', '#6366F1'
    ];
    const colors: string[] = [];
    for (let i = 0; i < count; i++) {
      colors.push(baseColors[i % baseColors.length]);
    }
    return colors;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }
}
