export type ExpenseFrequency = 'MONTHLY' | 'QUARTERLY' | 'YEARLY' | 'ONE_TIME';

export interface ExpenseTemplate {
  id?: number;
  name: string;
  amount: number;
  categoryId?: number;
  categoryName?: string;
  startDate?: string;
  endDate?: string;
  dayOfMonth?: number;
  frequency: ExpenseFrequency;
  active: boolean;
  notes?: string;
  autoPay: boolean;
}

export interface FrequencyOption {
  value: string;
  label: string;
}
