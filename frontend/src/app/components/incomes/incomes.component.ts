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
}
