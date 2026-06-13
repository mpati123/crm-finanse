import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { Category } from '../../models/expense.model';
import { ExpenseTemplate, ExpenseFrequency, FrequencyOption } from '../../models/expense-template.model';

@Component({
  selector: 'app-expense-templates',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './expense-templates.component.html',
  styleUrl: './expense-templates.component.scss'
})
export class ExpenseTemplatesComponent implements OnInit {
  templates: ExpenseTemplate[] = [];
  filteredTemplates: ExpenseTemplate[] = [];
  categories: Category[] = [];
  frequencies: FrequencyOption[] = [];
  loading = false;
  showForm = false;
  editingTemplate: ExpenseTemplate | null = null;

  // Filtering
  filterText = '';
  filterCategory: number | null = null;
  filterFrequency: string | null = null;
  filterActiveOnly = false;

  // Sorting
  sortColumn: 'name' | 'amount' | 'frequency' | 'categoryName' | 'dayOfMonth' = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Pagination
  currentPage = 1;
  pageSize = 15;
  pageSizeOptions = [10, 15, 25, 50];

  newTemplate: ExpenseTemplate = this.getEmptyTemplate();

  constructor(private apiService: ApiService, private translate: TranslateService) {}

  ngOnInit(): void {
    this.loadTemplates();
    this.loadCategories();
    this.loadFrequencies();
  }

  getEmptyTemplate(): ExpenseTemplate {
    return {
      name: '',
      amount: 0,
      frequency: 'MONTHLY',
      active: true,
      autoPay: false,
      dayOfMonth: 1
    };
  }

  loadTemplates(): void {
    this.loading = true;
    this.apiService.getAllExpenseTemplates().subscribe({
      next: (data) => {
        this.templates = data;
        this.applyFiltersAndSort();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading expense templates:', err);
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

  loadFrequencies(): void {
    this.apiService.getExpenseFrequencies().subscribe({
      next: (data) => this.frequencies = data,
      error: (err) => console.error('Error loading frequencies:', err)
    });
  }

  // === Filtering ===
  applyFiltersAndSort(): void {
    let result = [...this.templates];

    if (this.filterText) {
      const searchLower = this.filterText.toLowerCase();
      result = result.filter(template =>
        template.name.toLowerCase().includes(searchLower) ||
        (template.categoryName && template.categoryName.toLowerCase().includes(searchLower))
      );
    }

    if (this.filterCategory) {
      result = result.filter(template => template.categoryId === this.filterCategory);
    }

    if (this.filterFrequency) {
      result = result.filter(template => template.frequency === this.filterFrequency);
    }

    if (this.filterActiveOnly) {
      result = result.filter(template => template.active);
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
        case 'frequency':
          valueA = a.frequency.toLowerCase();
          valueB = b.frequency.toLowerCase();
          break;
        case 'categoryName':
          valueA = (a.categoryName || '').toLowerCase();
          valueB = (b.categoryName || '').toLowerCase();
          break;
        case 'dayOfMonth':
          valueA = a.dayOfMonth || 0;
          valueB = b.dayOfMonth || 0;
          break;
        default:
          return 0;
      }

      if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredTemplates = result;

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
    this.filterFrequency = null;
    this.filterActiveOnly = false;
    this.currentPage = 1;
    this.applyFiltersAndSort();
  }

  // === Sorting ===
  sort(column: 'name' | 'amount' | 'frequency' | 'categoryName' | 'dayOfMonth'): void {
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
  get paginatedTemplates(): ExpenseTemplate[] {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredTemplates.slice(start, end);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredTemplates.length / this.pageSize);
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

  openForm(template?: ExpenseTemplate): void {
    if (template) {
      this.editingTemplate = template;
      this.newTemplate = { ...template };
    } else {
      this.editingTemplate = null;
      this.newTemplate = this.getEmptyTemplate();
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingTemplate = null;
  }

  saveTemplate(): void {
    if (this.editingTemplate?.id) {
      this.apiService.updateExpenseTemplate(this.editingTemplate.id, this.newTemplate).subscribe({
        next: () => {
          this.loadTemplates();
          this.closeForm();
        },
        error: (err) => console.error('Error updating expense template:', err)
      });
    } else {
      this.apiService.createExpenseTemplate(this.newTemplate).subscribe({
        next: () => {
          this.loadTemplates();
          this.closeForm();
        },
        error: (err) => console.error('Error creating expense template:', err)
      });
    }
  }

  deleteTemplate(id: number): void {
    if (confirm(this.translate.instant('EXPENSE_TEMPLATES.DELETE_CONFIRM'))) {
      this.apiService.deleteExpenseTemplate(id).subscribe({
        next: () => this.loadTemplates(),
        error: (err) => console.error('Error deleting expense template:', err)
      });
    }
  }

  toggleActive(template: ExpenseTemplate): void {
    const updated = { ...template, active: !template.active };
    this.apiService.updateExpenseTemplate(template.id!, updated).subscribe({
      next: () => this.loadTemplates(),
      error: (err) => console.error('Error toggling active status:', err)
    });
  }

  getFrequencyLabel(freq: ExpenseFrequency): string {
    const found = this.frequencies.find(f => f.value === freq);
    return found ? found.label : freq;
  }

  getActiveCount(): number {
    return this.templates.filter(t => t.active).length;
  }

  getTotalMonthly(): number {
    return this.templates
      .filter(t => t.active)
      .reduce((sum, t) => {
        let monthly = t.amount;
        switch (t.frequency) {
          case 'QUARTERLY':
            monthly = t.amount / 3;
            break;
          case 'YEARLY':
            monthly = t.amount / 12;
            break;
          case 'ONE_TIME':
            monthly = 0;
            break;
        }
        return sum + monthly;
      }, 0);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }

  generateExpenses(): void {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;

    this.apiService.generateExpensesForMonth(year, month).subscribe({
      next: (result) => {
        alert(this.translate.instant('EXPENSES.GENERATED_SUCCESS', { count: result.length }));
      },
      error: (err) => console.error('Error generating expenses:', err)
    });
  }
}
