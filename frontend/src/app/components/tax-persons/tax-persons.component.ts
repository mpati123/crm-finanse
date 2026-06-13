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
  loading = false;
  showModal = false;
  editMode = false;
  currentTaxPerson: TaxPerson | null = null;

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
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading tax persons:', error);
        this.loading = false;
      }
    });
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
