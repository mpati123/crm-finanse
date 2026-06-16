import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { PlannedPurchase, PRIORITY_OPTIONS, STATUS_OPTIONS, PurchasePriority, PurchaseStatus } from '../../models/planned-purchase.model';
import { Category } from '../../models/expense.model';

@Component({
  selector: 'app-planned-purchases',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './planned-purchases.component.html',
  styleUrl: './planned-purchases.component.scss'
})
export class PlannedPurchasesComponent implements OnInit {
  purchases: PlannedPurchase[] = [];
  filteredPurchases: PlannedPurchase[] = [];
  categories: Category[] = [];
  selectedYear: number = new Date().getFullYear();
  loading = false;
  showForm = false;
  editingPurchase: PlannedPurchase | null = null;

  priorityOptions = PRIORITY_OPTIONS;
  statusOptions = STATUS_OPTIONS;

  // Filtering
  filterText = '';
  filterStatus: PurchaseStatus | null = null;
  filterPriority: PurchasePriority | null = null;
  filterMonth: number | null = null;

  // Sorting
  sortColumn: 'name' | 'amount' | 'plannedMonth' | 'priority' | 'status' = 'plannedMonth';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Pagination
  currentPage = 1;
  pageSize = 25;
  pageSizeOptions = [10, 15, 25, 50];

  newPurchase: PlannedPurchase = {
    name: '',
    amount: 0,
    plannedYear: this.selectedYear,
    plannedMonth: new Date().getMonth() + 1,
    priority: 'MEDIUM',
    status: 'PLANNED'
  };

  months = [
    { value: 1, label: 'Styczeń' },
    { value: 2, label: 'Luty' },
    { value: 3, label: 'Marzec' },
    { value: 4, label: 'Kwiecień' },
    { value: 5, label: 'Maj' },
    { value: 6, label: 'Czerwiec' },
    { value: 7, label: 'Lipiec' },
    { value: 8, label: 'Sierpień' },
    { value: 9, label: 'Wrzesień' },
    { value: 10, label: 'Październik' },
    { value: 11, label: 'Listopad' },
    { value: 12, label: 'Grudzień' }
  ];

  years: number[] = [];

  constructor(private apiService: ApiService, private translate: TranslateService) {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear + 2; i >= currentYear - 2; i--) {
      this.years.push(i);
    }
  }

  ngOnInit(): void {
    this.loadPurchases();
    this.loadCategories();
  }

  loadPurchases(): void {
    this.loading = true;
    this.apiService.getPlannedPurchasesByYear(this.selectedYear).subscribe({
      next: (data) => {
        this.purchases = data;
        this.applyFiltersAndSort();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading planned purchases:', err);
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

  onYearChange(): void {
    this.currentPage = 1;
    this.newPurchase.plannedYear = this.selectedYear;
    this.loadPurchases();
  }

  // === Filtering ===
  applyFiltersAndSort(): void {
    let result = [...this.purchases];

    if (this.filterText) {
      const searchLower = this.filterText.toLowerCase();
      result = result.filter(p =>
        p.name.toLowerCase().includes(searchLower) ||
        (p.description && p.description.toLowerCase().includes(searchLower)) ||
        (p.categoryName && p.categoryName.toLowerCase().includes(searchLower))
      );
    }

    if (this.filterStatus) {
      result = result.filter(p => p.status === this.filterStatus);
    }

    if (this.filterPriority) {
      result = result.filter(p => p.priority === this.filterPriority);
    }

    if (this.filterMonth) {
      result = result.filter(p => p.plannedMonth === this.filterMonth);
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
        case 'plannedMonth':
          valueA = a.plannedMonth;
          valueB = b.plannedMonth;
          break;
        case 'priority':
          const priorityOrder = { 'URGENT': 0, 'HIGH': 1, 'MEDIUM': 2, 'LOW': 3 };
          valueA = priorityOrder[a.priority];
          valueB = priorityOrder[b.priority];
          break;
        case 'status':
          valueA = a.status;
          valueB = b.status;
          break;
        default:
          return 0;
      }

      if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredPurchases = result;

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
    this.filterStatus = null;
    this.filterPriority = null;
    this.filterMonth = null;
    this.currentPage = 1;
    this.applyFiltersAndSort();
  }

  // === Sorting ===
  sort(column: 'name' | 'amount' | 'plannedMonth' | 'priority' | 'status'): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applyFiltersAndSort();
  }

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) return '↕️';
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  // === Pagination ===
  get paginatedPurchases(): PlannedPurchase[] {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredPurchases.slice(start, end);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredPurchases.length / this.pageSize);
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

  openForm(purchase?: PlannedPurchase): void {
    if (purchase) {
      this.editingPurchase = purchase;
      this.newPurchase = { ...purchase };
    } else {
      this.editingPurchase = null;
      this.newPurchase = {
        name: '',
        amount: 0,
        plannedYear: this.selectedYear,
        plannedMonth: new Date().getMonth() + 1,
        priority: 'MEDIUM',
        status: 'PLANNED'
      };
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingPurchase = null;
  }

  savePurchase(): void {
    if (this.editingPurchase?.id) {
      this.apiService.updatePlannedPurchase(this.editingPurchase.id, this.newPurchase).subscribe({
        next: () => {
          this.loadPurchases();
          this.closeForm();
        },
        error: (err) => console.error('Error updating purchase:', err)
      });
    } else {
      this.apiService.createPlannedPurchase(this.newPurchase).subscribe({
        next: () => {
          this.loadPurchases();
          this.closeForm();
        },
        error: (err) => console.error('Error creating purchase:', err)
      });
    }
  }

  deletePurchase(id: number): void {
    if (confirm('Czy na pewno chcesz usunąć ten zaplanowany zakup?')) {
      this.apiService.deletePlannedPurchase(id).subscribe({
        next: () => this.loadPurchases(),
        error: (err) => console.error('Error deleting purchase:', err)
      });
    }
  }

  markAsPurchased(id: number): void {
    if (confirm('Czy na pewno chcesz oznaczyć jako kupiony? Zostanie utworzony wydatek.')) {
      this.apiService.markPurchaseAsPurchased(id).subscribe({
        next: () => this.loadPurchases(),
        error: (err) => console.error('Error marking as purchased:', err)
      });
    }
  }

  getTotalPlanned(): number {
    return this.filteredPurchases
      .filter(p => p.status === 'PLANNED')
      .reduce((sum, p) => sum + p.amount, 0);
  }

  getTotalPurchased(): number {
    return this.filteredPurchases
      .filter(p => p.status === 'PURCHASED')
      .reduce((sum, p) => sum + p.amount, 0);
  }

  getMonthName(month: number): string {
    return this.months.find(m => m.value === month)?.label || '';
  }

  getPriorityLabel(priority: PurchasePriority): string {
    return this.priorityOptions.find(p => p.value === priority)?.label || priority;
  }

  getStatusLabel(status: PurchaseStatus): string {
    return this.statusOptions.find(s => s.value === status)?.label || status;
  }

  getPriorityClass(priority: PurchasePriority): string {
    return `priority-${priority.toLowerCase()}`;
  }

  getStatusClass(status: PurchaseStatus): string {
    return `status-${status.toLowerCase()}`;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }
}
