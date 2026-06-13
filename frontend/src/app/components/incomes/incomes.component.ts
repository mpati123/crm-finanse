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
  filteredIncomes: Income[] = [];
  categories: Category[] = [];
  taxPersons: any[] = [];
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  loading = false;
  showForm = false;
  editingIncome: Income | null = null;
  yearToDateIncomes: Income[] = [];
  incomeSources: any[] = [];
  uniqueIncomeSources: Array<{id: number, name: string, total?: number, gross?: number, net?: number, personName?: string, isB2B?: boolean}> = [];

  // Filtering
  filterText = '';
  filterCategory: number | null = null;
  filterTaxPerson: number | null = null;

  // Sorting
  sortColumn: 'name' | 'amount' | 'date' | 'categoryName' | 'taxPersonName' = 'date';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Pagination
  currentPage = 1;
  pageSize = 15;
  pageSizeOptions = [10, 15, 25, 50];

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
    this.loadTaxPersons();
  }

  loadIncomes(): void {
    this.loading = true;
    this.apiService.generateIncomesForMonth(this.selectedYear, this.selectedMonth).subscribe({
      next: () => {
        this.fetchIncomes();
      },
      error: () => {
        this.fetchIncomes();
      }
    });
  }

  private fetchIncomes(): void {
    this.apiService.getIncomesByMonth(this.selectedYear, this.selectedMonth).subscribe({
      next: (data) => {
        this.incomes = data;
        this.applyFiltersAndSort();
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
    this.currentPage = 1;
    this.loadIncomes();
    this.loadYearToDateIncomes();
  }

  loadIncomeSources(): void {
    this.apiService.getActiveIncomeSources().subscribe({
      next: (sources) => this.incomeSources = sources,
      error: (err) => console.error('Error loading income sources:', err)
    });
  }

  loadTaxPersons(): void {
    this.apiService.getActiveTaxPersons().subscribe({
      next: (persons) => this.taxPersons = persons,
      error: (err) => console.error('Error loading tax persons:', err)
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

  // === Filtering ===
  applyFiltersAndSort(): void {
    let result = [...this.incomes];

    if (this.filterText) {
      const searchLower = this.filterText.toLowerCase();
      result = result.filter(income =>
        income.name.toLowerCase().includes(searchLower) ||
        (income.categoryName && income.categoryName.toLowerCase().includes(searchLower)) ||
        (income.taxPersonName && income.taxPersonName.toLowerCase().includes(searchLower))
      );
    }

    if (this.filterCategory) {
      result = result.filter(income => income.categoryId === this.filterCategory);
    }

    if (this.filterTaxPerson) {
      result = result.filter(income => income.taxPersonId === this.filterTaxPerson);
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
        case 'taxPersonName':
          valueA = (a.taxPersonName || '').toLowerCase();
          valueB = (b.taxPersonName || '').toLowerCase();
          break;
        default:
          return 0;
      }

      if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredIncomes = result;

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
    this.filterTaxPerson = null;
    this.currentPage = 1;
    this.applyFiltersAndSort();
  }

  // === Sorting ===
  sort(column: 'name' | 'amount' | 'date' | 'categoryName' | 'taxPersonName'): void {
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
  get paginatedIncomes(): Income[] {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredIncomes.slice(start, end);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredIncomes.length / this.pageSize);
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
    return this.filteredIncomes.reduce((sum, i) => sum + i.amount, 0);
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
      const source = income.incomeSourceId ? this.incomeSources.find(s => s.id === income.incomeSourceId) : null;
      const isB2B = source?.incomeType === 'B2B';

      const taxPersonId = income.taxPersonId || source?.taxPersonId;
      const taxPersonName = income.taxPersonName || source?.taxPersonName;

      const key = taxPersonId ? `tax-${taxPersonId}` : `source-${income.incomeSourceId || 'unknown'}`;

      const existingEntry = sourcesMap.get(key);

      if (existingEntry) {
        if (isB2B) {
          existingEntry.gross = (existingEntry.gross || 0) + income.amount;
          existingEntry.net = (existingEntry.net || 0) + ((income as any).netAmount || 0);
        } else {
          existingEntry.total = (existingEntry.total || 0) + (income.actualAmount || income.amount);
        }
      } else {
        const displayName = taxPersonName || source?.name || income.name;

        const entryData: any = {
          id: taxPersonId || income.incomeSourceId || 0,
          name: displayName,
          personName: taxPersonName,
          isB2B
        };

        if (isB2B) {
          entryData.gross = income.amount;
          entryData.net = (income as any).netAmount || 0;
        } else {
          entryData.total = income.actualAmount || income.amount;
        }

        sourcesMap.set(key, entryData);
      }
    });

    this.uniqueIncomeSources = Array.from(sourcesMap.values());
  }

}
