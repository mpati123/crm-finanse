import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { Income, Category } from '../../models/expense.model';

@Component({
  selector: 'app-incomes',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './incomes.component.html',
  styleUrl: './incomes.component.scss'
})
export class IncomesComponent implements OnInit {
  incomes: Income[] = [];
  categories: Category[] = [];
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  loading = false;
  showForm = false;
  editingIncome: Income | null = null;
  yearToDateIncomes: Income[] = [];
  incomeSources: any[] = [];
  uniqueIncomeSources: Array<{id: number, name: string, total?: number, gross?: number, net?: number, personName?: string, isB2B?: boolean}> = [];

  newIncome: Income = {
    name: '',
    amount: 0,
    date: new Date().toISOString().split('T')[0],
    recurring: false,
    estimated: false,
    actualHours: undefined,
    overtimeHours100: 0,
    overtimeHours150: 0,
    overtimeHours200: 0
  };

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
    this.loadIncomes();
    this.loadIncomeSources();
    this.loadCategories();
  }

  loadIncomes(): void {
    this.loading = true;
    // Najpierw automatycznie generuj ze źródeł, potem pobierz dane
    this.apiService.generateIncomesForMonth(this.selectedYear, this.selectedMonth).subscribe({
      next: () => {
        this.fetchIncomes();
      },
      error: () => {
        // Jeśli generowanie nie zadziała, i tak pobierz istniejące dane
        this.fetchIncomes();
      }
    });
  }

  private fetchIncomes(): void {
    this.apiService.getIncomesByMonth(this.selectedYear, this.selectedMonth).subscribe({
      next: (data) => {
        this.incomes = data;
        this.loading = false;
        this.loadYearToDateIncomes();
      },
      error: (err) => {
        console.error('Error loading incomes:', err);
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.apiService.getCategoriesByType('INCOME').subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error('Error loading categories:', err)
    });
  }

  onMonthChange(): void {
    this.loadIncomes();
    this.loadYearToDateIncomes();
  }


  loadIncomeSources(): void {
    this.apiService.getActiveIncomeSources().subscribe({
      next: (sources) => this.incomeSources = sources,
      error: (err) => console.error('Error loading income sources:', err)
    });
  }

  loadYearToDateIncomes(): void {
    const startDate = `${this.selectedYear}-01-01`;
    const endMonth = this.selectedMonth.toString().padStart(2, '0');
    const lastDay = new Date(this.selectedYear, this.selectedMonth, 0).getDate();
    const endDate = `${this.selectedYear}-${endMonth}-${lastDay}`;

    this.apiService.getIncomes().subscribe({
      next: (allIncomes) => {
        this.yearToDateIncomes = allIncomes.filter(income => {
          if (!income.date) return false;
          const incomeDate = new Date(income.date);
          return incomeDate >= new Date(startDate) && incomeDate <= new Date(endDate);
        });
        this.calculateUniqueIncomeSources();
      },
      error: (err) => console.error('Error loading year-to-date incomes:', err)
    });
  }


  openForm(income?: Income): void {
    if (income) {
      this.editingIncome = income;
      this.newIncome = { ...income };
    } else {
      this.editingIncome = null;
      this.newIncome = {
        name: '',
        amount: 0,
        date: new Date().toISOString().split('T')[0],
        recurring: false,
        estimated: false,
        actualHours: undefined,
        overtimeHours100: 0,
        overtimeHours150: 0,
        overtimeHours200: 0
      };
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingIncome = null;
  }

  saveIncome(): void {
    if (this.editingIncome?.id) {
      this.apiService.updateIncome(this.editingIncome.id, this.newIncome).subscribe({
        next: () => {
          this.loadIncomes();
          this.closeForm();
        },
        error: (err) => console.error('Error updating income:', err)
      });
    } else {
      this.apiService.createIncome(this.newIncome).subscribe({
        next: () => {
          this.loadIncomes();
          this.closeForm();
        },
        error: (err) => console.error('Error creating income:', err)
      });
    }
  }

  deleteIncome(id: number): void {
    this.translate.get('INCOMES.DELETE_CONFIRM').subscribe(msg => {
      if (confirm(msg)) {
        this.apiService.deleteIncome(id).subscribe({
          next: () => this.loadIncomes(),
          error: (err) => console.error('Error deleting income:', err)
        });
      }
    });
  }

  getTotalAmount(): number {
    return this.incomes.reduce((sum, i) => sum + i.amount, 0);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }

  getCumulativeBySource(sourceId: number | undefined): number {
    if (!sourceId) return 0;
    return this.yearToDateIncomes
      .filter(i => i.incomeSourceId === sourceId)
      .reduce((sum, i) => sum + (i.actualAmount || i.amount), 0);
  }

  getYearToDateTotal(): number {
    return this.yearToDateIncomes.reduce((sum, i) => sum + (i.actualAmount || i.amount), 0);
  }

  calculateUniqueIncomeSources(): void {
    const sourcesMap = new Map<string, {id: number, name: string, total?: number, gross?: number, net?: number, personName?: string, isB2B?: boolean}>();

    this.yearToDateIncomes.forEach(income => {
      if (income.incomeSourceId) {
        const source = this.incomeSources.find(s => s.id === income.incomeSourceId);
        const isB2B = source?.incomeType === 'B2B';
        const personName = source?.personName;

        // Dla B2B tworzymy osobny klucz per osoba
        const key = isB2B && personName
          ? `${income.incomeSourceId}-${personName}`
          : `${income.incomeSourceId}`;

        const existingSource = sourcesMap.get(key);

        if (existingSource) {
          if (isB2B) {
            existingSource.gross = (existingSource.gross || 0) + income.amount;
            existingSource.net = (existingSource.net || 0) + ((income as any).netAmount || 0);
          } else {
            existingSource.total = (existingSource.total || 0) + (income.actualAmount || income.amount);
          }
        } else {
          const displayName = isB2B && personName
            ? `${source?.name || income.name} (${personName})`
            : source?.name || income.name;

          const sourceData: any = {
            id: income.incomeSourceId,
            name: displayName,
            personName,
            isB2B
          };

          if (isB2B) {
            sourceData.gross = income.amount;
            sourceData.net = (income as any).netAmount || 0;
          } else {
            sourceData.total = income.actualAmount || income.amount;
          }

          sourcesMap.set(key, sourceData);
        }
      }
    });

    this.uniqueIncomeSources = Array.from(sourcesMap.values());
  }

}
