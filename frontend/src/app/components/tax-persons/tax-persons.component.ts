import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { TaxPerson, CostTypeOption, TaxPersonType } from '../../models/tax-person.model';
import { EnumOption, ZUSTypeOption } from '../../models/income-source.model';

@Component({
  selector: 'app-tax-persons',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './tax-persons.component.html',
  styleUrls: ['./tax-persons.component.scss']
})
export class TaxPersonsComponent implements OnInit {
  taxPersons: TaxPerson[] = [];
  filteredTaxPersons: TaxPerson[] = [];
  loading = false;
  showModal = false;
  editMode = false;
  currentTaxPerson: TaxPerson | null = null;

  // Filtering
  filterText = '';
  filterType: 'ALL' | 'INDIVIDUAL' | 'COMPANY' = 'ALL';
  filterActiveOnly = false;

  // Sorting
  sortColumn: 'name' | 'type' | 'taxYear' | 'cumulativeGrossIncome' = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Options for dropdowns
  taxPersonTypes: { value: TaxPersonType; label: string }[] = [
    { value: 'INDIVIDUAL', label: 'INDIVIDUAL' },
    { value: 'COMPANY', label: 'COMPANY' }
  ];
  costTypes: CostTypeOption[] = [];
  taxForms: EnumOption[] = [];
  zusTypes: ZUSTypeOption[] = [];
  availableSpouses: TaxPerson[] = [];

  formData: TaxPerson = this.getEmptyTaxPerson();

  constructor(
    private apiService: ApiService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadTaxPersons();
    this.loadDropdownOptions();
  }

  getEmptyTaxPerson(): TaxPerson {
    const currentYear = new Date().getFullYear();
    return {
      type: 'INDIVIDUAL',
      name: '',
      taxForm: 'SKALA',
      pit2Filed: false,
      costType: 'STANDARD_250',
      jointTaxReturn: false,
      taxYear: currentYear,
      zusLimitReached: false,
      secondTaxBracket: false,
      defaultVatPayer: false,
      active: true
    };
  }

  loadTaxPersons(): void {
    this.loading = true;
    this.apiService.getAllTaxPersons().subscribe({
      next: (data) => {
        this.taxPersons = data;
        this.applyFiltersAndSort();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading tax persons:', error);
        this.loading = false;
      }
    });
  }

  // === Filtering & Sorting ===
  applyFiltersAndSort(): void {
    let result = [...this.taxPersons];

    // Text filter
    if (this.filterText) {
      const searchLower = this.filterText.toLowerCase();
      result = result.filter(person =>
        person.name.toLowerCase().includes(searchLower) ||
        (person.pesel && person.pesel.includes(this.filterText)) ||
        (person.nip && person.nip.includes(this.filterText))
      );
    }

    // Type filter
    if (this.filterType !== 'ALL') {
      result = result.filter(person => person.type === this.filterType);
    }

    // Active only filter
    if (this.filterActiveOnly) {
      result = result.filter(person => person.active);
    }

    // Sorting
    result.sort((a, b) => {
      let valueA: any;
      let valueB: any;

      switch (this.sortColumn) {
        case 'name':
          valueA = a.name.toLowerCase();
          valueB = b.name.toLowerCase();
          break;
        case 'type':
          valueA = a.type;
          valueB = b.type;
          break;
        case 'taxYear':
          valueA = a.taxYear || 0;
          valueB = b.taxYear || 0;
          break;
        case 'cumulativeGrossIncome':
          valueA = a.cumulativeGrossIncome || 0;
          valueB = b.cumulativeGrossIncome || 0;
          break;
        default:
          return 0;
      }

      if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredTaxPersons = result;
  }

  onFilterChange(): void {
    this.applyFiltersAndSort();
  }

  clearFilters(): void {
    this.filterText = '';
    this.filterType = 'ALL';
    this.filterActiveOnly = false;
    this.applyFiltersAndSort();
  }

  sort(column: 'name' | 'type' | 'taxYear' | 'cumulativeGrossIncome'): void {
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

  getActiveCount(): number {
    return this.taxPersons.filter(p => p.active).length;
  }

  loadDropdownOptions(): void {
    this.apiService.getCostTypes().subscribe({
      next: (data) => this.costTypes = data,
      error: (error) => console.error('Error loading cost types:', error)
    });

    this.apiService.getTaxForms().subscribe({
      next: (data) => this.taxForms = data,
      error: (error) => console.error('Error loading tax forms:', error)
    });

    this.apiService.getZUSTypes().subscribe({
      next: (data) => this.zusTypes = data,
      error: (error) => console.error('Error loading ZUS types:', error)
    });
  }

  openCreateModal(): void {
    this.editMode = false;
    this.currentTaxPerson = null;
    this.formData = this.getEmptyTaxPerson();
    this.updateAvailableSpouses();
    this.showModal = true;
  }

  openEditModal(taxPerson: TaxPerson): void {
    this.editMode = true;
    this.currentTaxPerson = taxPerson;
    this.formData = { ...taxPerson };
    this.updateAvailableSpouses();
    this.showModal = true;
  }

  updateAvailableSpouses(): void {
    this.availableSpouses = this.taxPersons.filter(
      tp => tp.id !== this.formData.id && tp.active
    );
  }

  closeModal(): void {
    this.showModal = false;
    this.currentTaxPerson = null;
    this.formData = this.getEmptyTaxPerson();
  }

  saveTaxPerson(): void {
    if (this.editMode && this.currentTaxPerson?.id) {
      this.apiService.updateTaxPerson(this.currentTaxPerson.id, this.formData).subscribe({
        next: () => {
          this.loadTaxPersons();
          this.closeModal();
        },
        error: (error) => console.error('Error updating tax person:', error)
      });
    } else {
      this.apiService.createTaxPerson(this.formData).subscribe({
        next: () => {
          this.loadTaxPersons();
          this.closeModal();
        },
        error: (error) => console.error('Error creating tax person:', error)
      });
    }
  }

  deleteTaxPerson(id: number): void {
    if (confirm(this.translate.instant('TAX_PERSONS.DELETE_CONFIRM'))) {
      this.apiService.deleteTaxPerson(id).subscribe({
        next: () => this.loadTaxPersons(),
        error: (error) => console.error('Error deleting tax person:', error)
      });
    }
  }

  resetYearlyTracking(id: number): void {
    const year = new Date().getFullYear();
    if (confirm(this.translate.instant('TAX_PERSONS.RESET_CONFIRM', { year }))) {
      this.apiService.resetYearlyTracking(id, year).subscribe({
        next: () => this.loadTaxPersons(),
        error: (error) => console.error('Error resetting yearly tracking:', error)
      });
    }
  }

  getCostTypeLabel(costType: string): string {
    const found = this.costTypes.find(ct => ct.value === costType);
    return found ? found.label : costType;
  }

  getTaxFormLabel(taxForm: string): string {
    const found = this.taxForms.find(tf => tf.value === taxForm);
    return found ? found.label : taxForm;
  }

  getZusTypeLabel(zusType: string): string {
    const found = this.zusTypes.find(zt => zt.value === zusType);
    return found ? found.label : zusType;
  }

  formatCurrency(value: number | undefined): string {
    if (value === undefined || value === null) return '0';
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN',
      minimumFractionDigits: 2
    }).format(value);
  }

  formatPercent(value: number | undefined): string {
    if (value === undefined || value === null) return '0%';
    return `${value.toFixed(1)}%`;
  }
}
