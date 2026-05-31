import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { Expense, Category } from '../../models/expense.model';

@Component({
  selector: 'app-expenses',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './expenses.component.html',
  styleUrl: './expenses.component.scss'
})
export class ExpensesComponent implements OnInit {
  expenses: Expense[] = [];
  categories: Category[] = [];
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  loading = false;
  showForm = false;
  editingExpense: Expense | null = null;

  newExpense: Expense = {
    name: '',
    amount: 0,
    date: new Date().toISOString().split('T')[0],
    status: 'PENDING',
    recurring: false
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
    this.loadExpenses();
    this.loadCategories();
  }

  loadExpenses(): void {
    this.loading = true;
    // Najpierw automatycznie generuj z szablonów, potem pobierz dane
    this.apiService.generateExpensesForMonth(this.selectedYear, this.selectedMonth).subscribe({
      next: () => {
        this.fetchExpenses();
      },
      error: () => {
        // Jeśli generowanie nie zadziała, i tak pobierz istniejące dane
        this.fetchExpenses();
      }
    });
  }

  private fetchExpenses(): void {
    this.apiService.getExpensesByMonth(this.selectedYear, this.selectedMonth).subscribe({
      next: (data) => {
        this.expenses = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading expenses:', err);
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.apiService.getCategoriesByType('EXPENSE').subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error('Error loading categories:', err)
    });
  }

  onMonthChange(): void {
    this.loadExpenses();
  }

  openForm(expense?: Expense): void {
    if (expense) {
      this.editingExpense = expense;
      this.newExpense = { ...expense };
    } else {
      this.editingExpense = null;
      this.newExpense = {
        name: '',
        amount: 0,
        date: new Date().toISOString().split('T')[0],
        status: 'PENDING',
        recurring: false
      };
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingExpense = null;
  }

  saveExpense(): void {
    if (this.editingExpense?.id) {
      this.apiService.updateExpense(this.editingExpense.id, this.newExpense).subscribe({
        next: () => {
          this.loadExpenses();
          this.closeForm();
        },
        error: (err) => console.error('Error updating expense:', err)
      });
    } else {
      this.apiService.createExpense(this.newExpense).subscribe({
        next: () => {
          this.loadExpenses();
          this.closeForm();
        },
        error: (err) => console.error('Error creating expense:', err)
      });
    }
  }

  deleteExpense(id: number): void {
    this.translate.get('EXPENSES.DELETE_CONFIRM').subscribe(msg => {
      if (confirm(msg)) {
        this.apiService.deleteExpense(id).subscribe({
          next: () => this.loadExpenses(),
          error: (err) => console.error('Error deleting expense:', err)
        });
      }
    });
  }

  toggleStatus(expense: Expense): void {
    const nextStatus: Record<string, 'PAID' | 'PENDING' | 'REMAINING'> = {
      'PENDING': 'PAID',
      'PAID': 'REMAINING',
      'REMAINING': 'PENDING'
    };
    const updated = { ...expense, status: nextStatus[expense.status] };
    this.apiService.updateExpense(expense.id!, updated).subscribe({
      next: () => this.loadExpenses(),
      error: (err) => console.error('Error updating status:', err)
    });
  }

  getStatusClass(status: string): string {
    return status.toLowerCase();
  }

  getTotalAmount(): number {
    return this.expenses.reduce((sum, e) => sum + e.amount, 0);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }
}
