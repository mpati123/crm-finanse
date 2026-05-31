export interface Expense {
  id?: number;
  name: string;
  amount: number;
  categoryId?: number;
  categoryName?: string;
  date: string;
  status: 'PAID' | 'PENDING' | 'REMAINING';
  notes?: string;
  recurring: boolean;
}

export interface Income {
  id?: number;
  name: string;
  amount: number;
  categoryId?: number;
  categoryName?: string;
  date: string;
  notes?: string;
  recurring: boolean;
  estimated: boolean;
  netAmount?: number;
  actualHours?: number;
  overtimeHours100?: number;
  overtimeHours150?: number;
  overtimeHours200?: number;
  incomeSourceId?: number;
  actualAmount?: number;
}

export interface Category {
  id?: number;
  name: string;
  color?: string;
  icon?: string;
  type: 'EXPENSE' | 'INCOME';
}

export interface Budget {
  id?: number;
  year: number;
  month: number;
  categoryId?: number;
  categoryName?: string;
  plannedAmount: number;
  actualAmount?: number;
  difference?: number;
  percentageUsed?: number;
}

export interface Dashboard {
  totalIncome: number;
  totalExpenses: number;
  balance: number;
  pendingPayments: number;
  yearlyIncome: number;
  yearlyExpenses: number;
  yearlyBalance: number;
  expensesByCategory: CategorySummary[];
  monthlyTrends: MonthlyTrend[];
}

export interface CategorySummary {
  categoryName: string;
  amount: number;
  percentage: number;
  color?: string;
}

export interface MonthlyTrend {
  year: number;
  month: number;
  monthName: string;
  income: number;
  expenses: number;
  balance: number;
}
