import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Budget, Category } from '../../models/expense.model';

@Component({
  selector: 'app-budget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './budget.component.html',
  styleUrl: './budget.component.scss'
})
export class BudgetComponent implements OnInit {
  Math = Math;
  budgets: Budget[] = [];
  categories: Category[] = [];
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  loading = false;
  showForm = false;
  editingBudget: Budget | null = null;

  newBudget: Budget = {
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    plannedAmount: 0
  };

  months = [
    { value: 1, name: 'Styczeń' },
    { value: 2, name: 'Luty' },
    { value: 3, name: 'Marzec' },
    { value: 4, name: 'Kwiecień' },
    { value: 5, name: 'Maj' },
    { value: 6, name: 'Czerwiec' },
    { value: 7, name: 'Lipiec' },
    { value: 8, name: 'Sierpień' },
    { value: 9, name: 'Wrzesień' },
    { value: 10, name: 'Październik' },
    { value: 11, name: 'Listopad' },
    { value: 12, name: 'Grudzień' }
  ];

  years: number[] = [];

  constructor(private apiService: ApiService) {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear + 1; i >= 2018; i--) {
      this.years.push(i);
    }
  }

  ngOnInit(): void {
    this.loadBudgets();
    this.loadCategories();
  }

  loadBudgets(): void {
    this.loading = true;
    this.apiService.getBudgetsWithActuals(this.selectedYear, this.selectedMonth).subscribe({
      next: (data) => {
        this.budgets = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading budgets:', err);
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
    this.loadBudgets();
  }

  openForm(budget?: Budget): void {
    if (budget) {
      this.editingBudget = budget;
      this.newBudget = { ...budget };
    } else {
      this.editingBudget = null;
      this.newBudget = {
        year: this.selectedYear,
        month: this.selectedMonth,
        plannedAmount: 0
      };
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingBudget = null;
  }

  saveBudget(): void {
    if (this.editingBudget?.id) {
      this.apiService.updateBudget(this.editingBudget.id, this.newBudget).subscribe({
        next: () => {
          this.loadBudgets();
          this.closeForm();
        },
        error: (err) => console.error('Error updating budget:', err)
      });
    } else {
      this.apiService.createBudget(this.newBudget).subscribe({
        next: () => {
          this.loadBudgets();
          this.closeForm();
        },
        error: (err) => console.error('Error creating budget:', err)
      });
    }
  }

  deleteBudget(id: number): void {
    if (confirm('Czy na pewno chcesz usunąć ten budżet?')) {
      this.apiService.deleteBudget(id).subscribe({
        next: () => this.loadBudgets(),
        error: (err) => console.error('Error deleting budget:', err)
      });
    }
  }

  getTotalPlanned(): number {
    return this.budgets.reduce((sum, b) => sum + b.plannedAmount, 0);
  }

  getTotalActual(): number {
    return this.budgets.reduce((sum, b) => sum + (b.actualAmount || 0), 0);
  }

  getProgressClass(budget: Budget): string {
    const percentage = budget.percentageUsed || 0;
    if (percentage >= 100) return 'over';
    if (percentage >= 80) return 'warning';
    return 'normal';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }
}
