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
  filteredExpenses: Expense[] = [];
  categories: Category[] = [];
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  loading = false;
  showForm = false;
  editingExpense: Expense | null = null;

  // Filtering
  filterText = '';
  filterCategory: number | null = null;
  filterStatus: string | null = null;

  // Sorting
  sortColumn: 'name' | 'amount' | 'date' | 'categoryName' | 'status' = 'date';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Pagination
  currentPage = 1;
  pageSize = 15;
  pageSizeOptions = [10, 15, 25, 50];

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
    this.apiService.generateExpensesForMonth(this.selectedYear, this.selectedMonth).subscribe({
      next: () => {
        this.fetchExpenses();
      },
      error: () => {
        this.fetchExpenses();
      }
    });
  }

  private fetchExpenses(): void {
    this.apiService.getExpensesByMonth(this.selectedYear, this.selectedMonth).subscribe({
      next: (data) => {
        this.expenses = data;
        this.applyFiltersAndSort();
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
    this.currentPage = 1;
    this.loadExpenses();
  }

  // === Filtering ===
  applyFiltersAndSort(): void {
    let result = [...this.expenses];

    if (this.filterText) {
      const searchLower = this.filterText.toLowerCase();
      result = result.filter(expense =>
        expense.name.toLowerCase().includes(searchLower) ||
        (expense.categoryName && expense.categoryName.toLowerCase().includes(searchLower))
      );
    }

    if (this.filterCategory) {
      result = result.filter(expense => expense.categoryId === this.filterCategory);
    }

    if (this.filterStatus) {
      result = result.filter(expense => expense.status === this.filterStatus);
    }

    result.sort((a, b) => {
      let valueA: any;
      let valueB: any;

      switch (this.sortColumn) {
        case 'name':
          valueA = a.name.toLowerCase();
          valueB = b.name.toLowerCase();
          break;
        case 'amount':
          valueA = a.amount;
          valueB = b.amount;
          break;
        case 'date':
          valueA = new Date(a.date).getTime();
          valueB = new Date(b.date).getTime();
          break;
        case 'categoryName':
          valueA = (a.categoryName || '').toLowerCase();
          valueB = (b.categoryName || '').toLowerCase();
          break;
        case 'status':
          valueA = a.status.toLowerCase();
          valueB = b.status.toLowerCase();
          break;
        default:
          return 0;
      }

      if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredExpenses = result;

    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = 1;
    }
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.applyFiltersAndSort();
  }

  clearFilters(): void {
    this.filterText = '';
    this.filterCategory = null;
    this.filterStatus = null;
    this.currentPage = 1;
    this.applyFiltersAndSort();
  }

  // === Sorting ===
  sort(column: 'name' | 'amount' | 'date' | 'categoryName' | 'status'): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applyFiltersAndSort();
  }

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) return '';
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  // === Pagination ===
  get paginatedExpenses(): Expense[] {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredExpenses.slice(start, end);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredExpenses.length / this.pageSize);
  }

  get pageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    let start = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
    let end = Math.min(this.totalPages, start + maxVisiblePages - 1);

    if (end - start + 1 < maxVisiblePages) {
      start = Math.max(1, end - maxVisiblePages + 1);
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
    this.applyFiltersAndSort();
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
    return this.filteredExpenses.reduce((sum, e) => sum + e.amount, 0);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }
}
