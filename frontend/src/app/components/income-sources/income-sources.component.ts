import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { Category } from '../../models/expense.model';
import {
  IncomeSource,
  IncomeType,
  TaxForm,
  ZUSType,
  AmountType,
  RateType,
  B2BConfig,
  UoPConfig,
  EnumOption,
  ZUSTypeOption
} from '../../models/income-source.model';

@Component({
  selector: 'app-income-sources',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './income-sources.component.html',
  styleUrl: './income-sources.component.scss'
})
export class IncomeSourcesComponent implements OnInit {
  incomeSources: IncomeSource[] = [];
  categories: Category[] = [];
  incomeTypes: EnumOption[] = [];
  taxForms: EnumOption[] = [];
  zusTypes: ZUSTypeOption[] = [];
  loading = false;
  showForm = false;
  editingSource: IncomeSource | null = null;
  showYearlySimulation = false;
  yearlySimulationData: number[] = [];
  taxScenariosData: {[key: string]: number[]} = {};
  selectedSourceForSimulation: IncomeSource | null = null;
  simulationYear: number = new Date().getFullYear();
  years: number[] = this.generateYearsArray();
  showComparisonView = false;

  newSource: IncomeSource = this.getEmptySource();

  constructor(
    private apiService: ApiService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadIncomeSources();
    this.loadCategories();
    this.loadEnums();
  }

  amountTypes: EnumOption[] = [
    { value: 'GROSS', label: 'Brutto' },
    { value: 'NET', label: 'Netto' },
    { value: 'FIXED', label: 'Kwota stala' }
  ];

  rateTypes: EnumOption[] = [
    { value: 'MONTHLY', label: 'Miesiecznie' },
    { value: 'HOURLY', label: 'Godzinowo' }
  ];

  getEmptySource(): IncomeSource {
    return {
      name: '',
      incomeType: 'UOP',
      amount: 0,
      amountType: 'GROSS',
      rateType: 'MONTHLY',
      active: true,
      b2bConfig: undefined,
      uopConfig: undefined
    };
  }

  getEmptyB2BConfig(): B2BConfig {
    return {
      taxForm: 'SKALA',
      zusType: 'PELNY',
      vatPayer: false,
      vatRate: 23
    };
  }

  getEmptyUoPConfig(): UoPConfig {
    return {
      ppk: false,
      ppkRate: 2,
      authorCosts: false,
      authorCostsPercentage: 50
    };
  }

  loadIncomeSources(): void {
    this.loading = true;
    this.apiService.getAllIncomeSources().subscribe({
      next: (data) => {
        this.incomeSources = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading income sources:', err);
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

  loadEnums(): void {
    this.apiService.getIncomeTypes().subscribe({
      next: (data) => this.incomeTypes = data,
      error: (err) => console.error('Error loading income types:', err)
    });
    this.apiService.getTaxForms().subscribe({
      next: (data) => this.taxForms = data,
      error: (err) => console.error('Error loading tax forms:', err)
    });
    this.apiService.getZUSTypes().subscribe({
      next: (data) => this.zusTypes = data,
      error: (err) => console.error('Error loading ZUS types:', err)
    });
  }

  onIncomeTypeChange(): void {
    if (this.newSource.incomeType === 'B2B') {
      this.newSource.b2bConfig = this.getEmptyB2BConfig();
      this.newSource.uopConfig = undefined;
    } else if (this.newSource.incomeType === 'UOP') {
      this.newSource.uopConfig = this.getEmptyUoPConfig();
      this.newSource.b2bConfig = undefined;
    } else {
      this.newSource.b2bConfig = undefined;
      this.newSource.uopConfig = undefined;
    }
    this.onIncomeTypeChangeForAmountType();
  }

  onZusTypeChange(): void {
    if (this.newSource.b2bConfig) {
      const selectedZus = this.zusTypes.find(z => z.value === this.newSource.b2bConfig?.zusType);
      if (selectedZus) {
        this.newSource.b2bConfig.zusAmount = selectedZus.defaultAmount;
      }
    }
  }

  openForm(source?: IncomeSource): void {
    if (source) {
      this.editingSource = source;
      this.newSource = JSON.parse(JSON.stringify(source));
    } else {
      this.editingSource = null;
      this.newSource = this.getEmptySource();
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingSource = null;
  }

  saveSource(): void {
    // Dla stawki godzinowej automatycznie oblicz kwotę
    if (this.isHourlyRate() && this.newSource.hourlyRate && this.newSource.defaultHoursPerMonth) {
      this.newSource.amount = this.getCalculatedMonthlyAmount();
    }

    if (this.editingSource?.id) {
      this.apiService.updateIncomeSource(this.editingSource.id, this.newSource).subscribe({
        next: () => {
          this.loadIncomeSources();
          this.closeForm();
        },
        error: (err) => console.error('Error updating income source:', err)
      });
    } else {
      this.apiService.createIncomeSource(this.newSource).subscribe({
        next: () => {
          this.loadIncomeSources();
          this.closeForm();
        },
        error: (err) => console.error('Error creating income source:', err)
      });
    }
  }

  deleteSource(id: number): void {
    if (confirm(this.translate.instant('INCOME_SOURCES.DELETE_CONFIRM'))) {
      this.apiService.deleteIncomeSource(id).subscribe({
        next: () => this.loadIncomeSources(),
        error: (err) => console.error('Error deleting income source:', err)
      });
    }
  }

  toggleActive(source: IncomeSource): void {
    const updated = { ...source, active: !source.active };
    this.apiService.updateIncomeSource(source.id!, updated).subscribe({
      next: () => this.loadIncomeSources(),
      error: (err) => console.error('Error toggling active status:', err)
    });
  }

  getActiveCount(): number {
    return this.incomeSources.filter(s => s.active).length;
  }

  getIncomeTypeLabel(type: IncomeType): string {
    const found = this.incomeTypes.find(t => t.value === type);
    return found ? found.label : type;
  }

  getTotalGross(): number {
    // Suma brutto z VAT (dla B2B vatPayer uzywa grossAmount, dla reszty amount)
    return this.incomeSources
      .filter(s => s.active)
      .reduce((sum, s) => {
        if (s.incomeType === 'B2B' && s.b2bConfig?.vatPayer && s.grossAmount) {
          return sum + s.grossAmount;
        }
        return sum + s.amount;
      }, 0);
  }

  getTotalNet(): number {
    // Suma netto fakturowego - kwota wprowadzona (amount)
    return this.incomeSources
      .filter(s => s.active)
      .reduce((sum, s) => sum + s.amount, 0);
  }

  getTotalOnHand(): number {
    // Suma "na reke" - po odliczeniu wszystkich skladek i podatkow
    return this.incomeSources
      .filter(s => s.active)
      .reduce((sum, s) => {
        // Dla SWIADCZENIE/CZYNSZ/INNE - kwota bez odliczen
        if (s.amountType === 'FIXED' || s.incomeType === 'SWIADCZENIE' || s.incomeType === 'CZYNSZ' || s.incomeType === 'INNE') {
          return sum + s.amount;
        }
        // Dla pozostalych - netAmount (kwota po odliczeniach ZUS, zdrowotnej, podatku)
        return sum + (s.netAmount || s.amount);
      }, 0);
  }

  onIncomeTypeChangeForAmountType(): void {
    // Dla swiadczen automatycznie ustawiamy FIXED
    if (this.newSource.incomeType === 'SWIADCZENIE') {
      this.newSource.amountType = 'FIXED';
    }
  }

  showAmountTypeSelector(): boolean {
    return this.newSource.incomeType !== 'SWIADCZENIE';
  }

  getAmountLabel(): string {
    if (this.newSource.incomeType === 'SWIADCZENIE') {
      return this.translate.instant('COMMON.AMOUNT');
    }
    const amountTypeKey = `AMOUNT_TYPES.${this.newSource.amountType}`;
    const amountTypeLabel = this.translate.instant(amountTypeKey);
    return `${this.translate.instant('COMMON.AMOUNT')} ${amountTypeLabel.toLowerCase()}`;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }

  generateIncomes(): void {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;

    this.apiService.generateIncomesForMonth(year, month).subscribe({
      next: (result) => {
        alert(this.translate.instant('INCOME_SOURCES.GENERATED_SUCCESS', { count: result.length, month, year }));
      },
      error: (err) => console.error('Error generating incomes:', err)
    });
  }

  isHourlyRate(): boolean {
    return this.newSource.rateType === 'HOURLY';
  }

  showHourlyRateFields(): boolean {
    const allowedTypes: IncomeType[] = ['B2B', 'UMOWA_ZLECENIE', 'UMOWA_O_DZIELO'];
    return allowedTypes.includes(this.newSource.incomeType);
  }

  onRateTypeChange(): void {
    if (this.newSource.rateType === 'HOURLY') {
      this.newSource.hourlyRate = this.newSource.hourlyRate || 0;
      this.newSource.defaultHoursPerMonth = this.newSource.defaultHoursPerMonth || 168;
    }
  }

  getCalculatedMonthlyAmount(): number {
    if (this.newSource.rateType === 'HOURLY' && this.newSource.hourlyRate && this.newSource.defaultHoursPerMonth) {
      return this.newSource.hourlyRate * this.newSource.defaultHoursPerMonth;
    }
    return this.newSource.amount;
  }

  getEmploymentFractionLabel(source: IncomeSource): string {
    if (!source.employmentFraction) return '';
    if (source.employmentFraction === 1) return this.translate.instant('EMPLOYMENT.FULL');
    if (source.employmentFraction === 0.5) return this.translate.instant('EMPLOYMENT.HALF');
    if (source.employmentFraction === 0.25) return this.translate.instant('EMPLOYMENT.QUARTER');
    if (source.employmentFraction === 0.75) return this.translate.instant('EMPLOYMENT.THREE_QUARTERS');
    return this.translate.instant('EMPLOYMENT.PERCENTAGE', { value: source.employmentFraction * 100 });
  }

  getAmountTypeLabel(source: IncomeSource): string {
    if (source.amountType === 'FIXED') {
      return this.translate.instant('COMMON.AMOUNT');
    }
    // Dla B2B z VAT - pokazujemy "Netto fakturowe"
    if (source.incomeType === 'B2B' && source.b2bConfig?.vatPayer) {
      return this.translate.instant('INCOME_SOURCES.NET_INVOICE');
    }
    // Dla pozostałych
    return source.amountType === 'NET'
      ? this.translate.instant('AMOUNT_TYPES.NET')
      : this.translate.instant('AMOUNT_TYPES.GROSS');
  }

  getRateTypeLabel(source: IncomeSource): string {
    if (source.rateType === 'HOURLY' && source.hourlyRate) {
      return `${this.formatCurrency(source.hourlyRate)}${this.translate.instant('HOURLY_RATE.PER_HOUR')}`;
    }
    return '';
  }

  openYearlySimulation(source: IncomeSource): void {
    if (!source.id) return;
    this.selectedSourceForSimulation = source;
    this.simulationYear = new Date().getFullYear();
    this.loadYearlySimulation(source.id, this.simulationYear);
  }

  loadYearlySimulation(sourceId: number, year: number): void {
    // Load both yearly simulation and tax scenarios
    this.apiService.getYearlySimulation(sourceId, year).subscribe({
      next: (data) => {
        this.yearlySimulationData = data;
      },
      error: (err) => console.error('Error loading yearly simulation:', err)
    });

    this.apiService.getTaxScenarios(sourceId, year).subscribe({
      next: (data) => {
        console.log('Tax scenarios data received:', data);
        console.log('Scenario keys:', Object.keys(data));
        this.taxScenariosData = data;
        this.showYearlySimulation = true;
      },
      error: (err) => console.error('Error loading tax scenarios:', err)
    });
  }

  closeYearlySimulation(): void {
    this.showYearlySimulation = false;
    this.selectedSourceForSimulation = null;
    this.showComparisonView = false;
  }

  toggleComparisonView(): void {
    this.showComparisonView = !this.showComparisonView;
  }

  onSimulationYearChange(): void {
    if (this.selectedSourceForSimulation?.id) {
      this.loadYearlySimulation(this.selectedSourceForSimulation.id, this.simulationYear);
    }
  }

  getMonthlyTotal(monthIndex: number): number {
    return this.yearlySimulationData[monthIndex] || 0;
  }

  getYearlyTotal(): number {
    return this.yearlySimulationData.reduce((sum, val) => sum + (val || 0), 0);
  }

  getMonthName(monthIndex: number): string {
    const monthKeys = [
      'JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE',
      'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER'
    ];
    return this.translate.instant(`MONTHS.${monthKeys[monthIndex]}`);
  }

  generateYearsArray(): number[] {
    const currentYear = new Date().getFullYear();
    const years = [];
    for (let i = -2; i <= 2; i++) {
      years.push(currentYear + i);
    }
    return years;
  }

  getScenarioKeys(): string[] {
    return Object.keys(this.taxScenariosData);
  }

  getScenarioTotal(scenarioKey: string): number {
    const data = this.taxScenariosData[scenarioKey];
    return data ? data.reduce((sum, val) => sum + (val || 0), 0) : 0;
  }

  getScenarioMonthly(scenarioKey: string, monthIndex: number): number {
    const data = this.taxScenariosData[scenarioKey];
    return data ? (data[monthIndex] || 0) : 0;
  }

  getScenarioName(scenarioKey: string): string {
    const translationKey = `TAX_SCENARIOS.${scenarioKey}`;
    const translated = this.translate.instant(translationKey);
    // Fallback to the key itself if translation not found
    return translated !== translationKey ? translated : scenarioKey;
  }

  getIKZELimit(year: number): number {
    // IKZE limit w 2026: 9388,20 PLN
    const limits: {[key: number]: number} = {
      2024: 9388.20,
      2025: 9388.20,
      2026: 9388.20
    };
    return limits[year] || 9388.20;
  }

  getIKELimit(year: number): number {
    // IKE limit w 2026: 23 472 PLN
    const limits: {[key: number]: number} = {
      2024: 23472,
      2025: 23472,
      2026: 23472
    };
    return limits[year] || 23472;
  }

  getIKZETaxBenefit(): number {
    // Oszczędność podatkowa przy IKZE (skala 12% lub 32%)
    const yearlyTotal = this.getYearlyTotal();
    // Załóżmy 12% dla uproszczenia, w rzeczywistości zależy od progu podatkowego
    return this.getIKZELimit(this.simulationYear) * 0.12;
  }

  getTaxFormLabel(taxForm: string): string {
    const found = this.taxForms.find(f => f.value === taxForm);
    return found ? found.label : taxForm;
  }

  getZusTypeLabel(zusType: string): string {
    const found = this.zusTypes.find(z => z.value === zusType);
    return found ? found.label : zusType;
  }
}
