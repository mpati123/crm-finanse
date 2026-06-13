import { TaxForm, ZUSType } from './income-source.model';

export type CostType = 'STANDARD_250' | 'INCREASED_300';
export type TaxPersonType = 'INDIVIDUAL' | 'COMPANY';

export interface TaxPerson {
  id?: number;
  type: TaxPersonType;

  // For INDIVIDUAL
  firstName?: string;
  lastName?: string;
  pesel?: string;

  // For COMPANY
  companyName?: string;
  nip?: string;

  // Computed field
  name: string;

  // PIT Configuration
  taxForm: TaxForm;
  pit2Filed: boolean;
  costType: CostType;
  jointTaxReturn: boolean;
  spouseId?: number;
  spouseName?: string;

  // Yearly tracking
  taxYear: number;
  cumulativeGrossIncome?: number;
  cumulativeTaxableIncome?: number;
  cumulativeZusPaid?: number;
  zusLimitReached: boolean;
  secondTaxBracket: boolean;

  // B2B defaults
  defaultTaxForm?: TaxForm;
  defaultZusType?: ZUSType;
  defaultVatPayer: boolean;

  active: boolean;

  // Calculated fields for display
  zusLimitProgress?: number;
  taxThresholdProgress?: number;
  remainingToZusLimit?: number;
  remainingToTaxThreshold?: number;
}

export interface CostTypeOption {
  value: CostType;
  label: string;
  amount: number;
}
