export interface SavingsGoal {
  id?: number;
  name: string;
  description?: string;
  targetAmount: number;
  currentAmount: number;
  targetDate?: string;
  monthlyContribution?: number;
  priority: GoalPriority;
  status: GoalStatus;
  icon?: string;
  color?: string;
  displayOrder?: number;
  createdAt?: string;
  updatedAt?: string;

  // Calculated fields from backend
  remainingAmount?: number;
  percentageComplete?: number;
  monthsToGoal?: number;
}

export type GoalPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type GoalStatus = 'ACTIVE' | 'COMPLETED' | 'PAUSED' | 'CANCELLED';

export const GOAL_PRIORITY_OPTIONS = [
  { value: 'LOW', label: 'Niski' },
  { value: 'MEDIUM', label: 'Średni' },
  { value: 'HIGH', label: 'Wysoki' }
];

export const GOAL_STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Aktywny' },
  { value: 'COMPLETED', label: 'Osiągnięty' },
  { value: 'PAUSED', label: 'Wstrzymany' },
  { value: 'CANCELLED', label: 'Anulowany' }
];

export const GOAL_ICONS = [
  { value: 'car', label: 'Samochód', emoji: '🚗' },
  { value: 'house', label: 'Dom', emoji: '🏠' },
  { value: 'vacation', label: 'Wakacje', emoji: '🏖️' },
  { value: 'laptop', label: 'Elektronika', emoji: '💻' },
  { value: 'ring', label: 'Biżuteria', emoji: '💍' },
  { value: 'education', label: 'Edukacja', emoji: '📚' },
  { value: 'health', label: 'Zdrowie', emoji: '🏥' },
  { value: 'emergency', label: 'Fundusz awaryjny', emoji: '🆘' },
  { value: 'retirement', label: 'Emerytura', emoji: '👴' },
  { value: 'other', label: 'Inne', emoji: '🎯' }
];
