export type IncomeType = 'UOP' | 'UMOWA_ZLECENIE' | 'UMOWA_O_DZIELO' | 'B2B' | 'SWIADCZENIE' | 'CZYNSZ' | 'INNE';
export type TaxForm = 'SKALA' | 'LINIOWY' | 'RYCZALT';
export type ZUSType = 'PELNY' | 'MALY_ZUS' | 'MALY_ZUS_PLUS' | 'PREFERENCYJNY' | 'ULGA_NA_START' | 'BEZ_ZUS';
export type AmountType = 'GROSS' | 'NET' | 'FIXED';
export type RateType = 'MONTHLY' | 'HOURLY';
export type CostRateType = 'STANDARD_20' | 'AUTHOR_50' | 'CUSTOM';

export interface B2BConfig {
  id?: number;
  taxForm: TaxForm;
  zusType: ZUSType;
  vatPayer: boolean;
  vatRate?: number;
  zusAmount?: number;
  healthInsurance?: number;
  incomeTaxAdvance?: number;
  ryczaltRate?: number;
}

export interface UoPConfig {
  id?: number;
  zusEmployee?: number;
  healthInsurance?: number;
  incomeTax?: number;
  ppk: boolean;
  ppkRate?: number;
  authorCosts: boolean;
  authorCostsPercentage?: number;
}

export interface UmowaZlecenieConfig {
  id?: number;
  withZus: boolean;
  zusAmount?: number;
  healthInsurance?: number;
  incomeTax?: number;
  costRateType: CostRateType;
  customCostRate?: number;
  pit2: boolean;
  ppk: boolean;
  ppkRate?: number;
}

export interface UmowaODzieloConfig {
  id?: number;
  incomeTax?: number;
  costRateType: CostRateType;
  customCostRate?: number;
}

export interface IncomeSource {
  id?: number;
  name: string;
  personName?: string;
  incomeType: IncomeType;
  amount: number;
  amountType: AmountType;
  rateType: RateType;
  hourlyRate?: number;
  defaultHoursPerMonth?: number;
  employmentFraction?: number;
  startDate?: string;
  endDate?: string;
  paymentDayOfMonth?: number;
  active: boolean;
  categoryId?: number;
  categoryName?: string;
  b2bConfig?: B2BConfig;
  uopConfig?: UoPConfig;
  umowaZlecenieConfig?: UmowaZlecenieConfig;
  umowaODzieloConfig?: UmowaODzieloConfig;
  taxPersonId?: number;
  taxPersonName?: string;
  notes?: string;
  // Wyliczone
  grossAmount?: number; // kwota brutto z VAT (dla B2B)
  netAmount?: number;
  totalDeductions?: number;
}

export interface EnumOption {
  value: string;
  label: string;
}

export interface ZUSTypeOption extends EnumOption {
  defaultAmount: number;
}
