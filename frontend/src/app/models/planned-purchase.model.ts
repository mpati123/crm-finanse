export interface PlannedPurchase {
  id?: number;
  name: string;
  description?: string;
  amount: number;
  categoryId?: number;
  categoryName?: string;
  plannedYear: number;
  plannedMonth: number;
  priority: PurchasePriority;
  status: PurchaseStatus;
  expenseId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export type PurchasePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type PurchaseStatus = 'PLANNED' | 'PURCHASED' | 'CANCELLED' | 'POSTPONED';

export const PRIORITY_OPTIONS = [
  { value: 'LOW', label: 'Niski' },
  { value: 'MEDIUM', label: 'Średni' },
  { value: 'HIGH', label: 'Wysoki' },
  { value: 'URGENT', label: 'Pilny' }
];

export const STATUS_OPTIONS = [
  { value: 'PLANNED', label: 'Zaplanowany' },
  { value: 'PURCHASED', label: 'Kupiony' },
  { value: 'CANCELLED', label: 'Anulowany' },
  { value: 'POSTPONED', label: 'Przełożony' }
];
