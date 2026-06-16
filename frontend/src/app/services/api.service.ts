import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Expense, Income, Category, Budget, Dashboard } from '../models/expense.model';
import { IncomeSource, EnumOption, ZUSTypeOption } from '../models/income-source.model';
import { ExpenseTemplate, FrequencyOption } from '../models/expense-template.model';
import { TaxPerson, CostTypeOption } from '../models/tax-person.model';
import { PlannedPurchase } from '../models/planned-purchase.model';
import { SavingsGoal } from '../models/savings-goal.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  // Dashboard
  getDashboard(year: number, month: number): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${this.baseUrl}/dashboard/${year}/${month}`);
  }

  // Expenses
  getAllExpenses(): Observable<Expense[]> {
    return this.http.get<Expense[]>(`${this.baseUrl}/expenses`);
  }

  getExpensesByMonth(year: number, month: number): Observable<Expense[]> {
    return this.http.get<Expense[]>(`${this.baseUrl}/expenses/month/${year}/${month}`);
  }

  createExpense(expense: Expense): Observable<Expense> {
    return this.http.post<Expense>(`${this.baseUrl}/expenses`, expense);
  }

  updateExpense(id: number, expense: Expense): Observable<Expense> {
    return this.http.put<Expense>(`${this.baseUrl}/expenses/${id}`, expense);
  }

  deleteExpense(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/expenses/${id}`);
  }

  // Incomes
  getAllIncomes(): Observable<Income[]> {
    return this.http.get<Income[]>(`${this.baseUrl}/incomes`);
  }

  getIncomes(): Observable<Income[]> {
    return this.http.get<Income[]>(`${this.baseUrl}/incomes`);
  }

    getIncomesByMonth(year: number, month: number): Observable<Income[]> {
    return this.http.get<Income[]>(`${this.baseUrl}/incomes/month/${year}/${month}`);
  }

  createIncome(income: Income): Observable<Income> {
    return this.http.post<Income>(`${this.baseUrl}/incomes`, income);
  }

  updateIncome(id: number, income: Income): Observable<Income> {
    return this.http.put<Income>(`${this.baseUrl}/incomes/${id}`, income);
  }

  deleteIncome(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/incomes/${id}`);
  }

  // Categories
  getAllCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`);
  }

  getCategoriesByType(type: string): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories/type/${type}`);
  }

  createCategory(category: Category): Observable<Category> {
    return this.http.post<Category>(`${this.baseUrl}/categories`, category);
  }

  updateCategory(id: number, category: Category): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/categories/${id}`, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/categories/${id}`);
  }

  // Budgets
  getBudgetsByMonth(year: number, month: number): Observable<Budget[]> {
    return this.http.get<Budget[]>(`${this.baseUrl}/budgets/month/${year}/${month}`);
  }

  getBudgetsWithActuals(year: number, month: number): Observable<Budget[]> {
    return this.http.get<Budget[]>(`${this.baseUrl}/budgets/month/${year}/${month}/actuals`);
  }

  createBudget(budget: Budget): Observable<Budget> {
    return this.http.post<Budget>(`${this.baseUrl}/budgets`, budget);
  }

  updateBudget(id: number, budget: Budget): Observable<Budget> {
    return this.http.put<Budget>(`${this.baseUrl}/budgets/${id}`, budget);
  }

  deleteBudget(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/budgets/${id}`);
  }

  // Import
  importExcel(file: File, sheetName: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('sheet', sheetName);
    return this.http.post(`${this.baseUrl}/import/excel`, formData);
  }

  // Income Sources
  getAllIncomeSources(): Observable<IncomeSource[]> {
    return this.http.get<IncomeSource[]>(`${this.baseUrl}/income-sources`);
  }

  getActiveIncomeSources(): Observable<IncomeSource[]> {
    return this.http.get<IncomeSource[]>(`${this.baseUrl}/income-sources/active`);
  }

  getIncomeSourceById(id: number): Observable<IncomeSource> {
    return this.http.get<IncomeSource>(`${this.baseUrl}/income-sources/${id}`);
  }

  createIncomeSource(source: IncomeSource): Observable<IncomeSource> {
    return this.http.post<IncomeSource>(`${this.baseUrl}/income-sources`, source);
  }

  updateIncomeSource(id: number, source: IncomeSource): Observable<IncomeSource> {
    return this.http.put<IncomeSource>(`${this.baseUrl}/income-sources/${id}`, source);
  }

  deleteIncomeSource(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/income-sources/${id}`);
  }

  getIncomeTypes(): Observable<EnumOption[]> {
    return this.http.get<EnumOption[]>(`${this.baseUrl}/income-sources/types`);
  }

  getTaxForms(): Observable<EnumOption[]> {
    return this.http.get<EnumOption[]>(`${this.baseUrl}/income-sources/tax-forms`);
  }

  getZUSTypes(): Observable<ZUSTypeOption[]> {
    return this.http.get<ZUSTypeOption[]>(`${this.baseUrl}/income-sources/zus-types`);
  }

  getAmountTypes(): Observable<EnumOption[]> {
    return this.http.get<EnumOption[]>(`${this.baseUrl}/income-sources/amount-types`);
  }

  // Expense Templates
  getAllExpenseTemplates(): Observable<ExpenseTemplate[]> {
    return this.http.get<ExpenseTemplate[]>(`${this.baseUrl}/expense-templates`);
  }

  getActiveExpenseTemplates(): Observable<ExpenseTemplate[]> {
    return this.http.get<ExpenseTemplate[]>(`${this.baseUrl}/expense-templates/active`);
  }

  getExpenseTemplateById(id: number): Observable<ExpenseTemplate> {
    return this.http.get<ExpenseTemplate>(`${this.baseUrl}/expense-templates/${id}`);
  }

  createExpenseTemplate(template: ExpenseTemplate): Observable<ExpenseTemplate> {
    return this.http.post<ExpenseTemplate>(`${this.baseUrl}/expense-templates`, template);
  }

  updateExpenseTemplate(id: number, template: ExpenseTemplate): Observable<ExpenseTemplate> {
    return this.http.put<ExpenseTemplate>(`${this.baseUrl}/expense-templates/${id}`, template);
  }

  deleteExpenseTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/expense-templates/${id}`);
  }

  getExpenseFrequencies(): Observable<FrequencyOption[]> {
    return this.http.get<FrequencyOption[]>(`${this.baseUrl}/expense-templates/frequencies`);
  }

  // Recurring Transactions
  generateTransactionsForMonth(year: number, month: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/recurring/generate/${year}/${month}`, {});
  }

  generateIncomesForMonth(year: number, month: number): Observable<Income[]> {
    return this.http.post<Income[]>(`${this.baseUrl}/recurring/generate/incomes/${year}/${month}`, {});
  }

  generateExpensesForMonth(year: number, month: number): Observable<Expense[]> {
    return this.http.post<Expense[]>(`${this.baseUrl}/recurring/generate/expenses/${year}/${month}`, {});
  }

  // Yearly income simulation
  getYearlySimulation(sourceId: number, year?: number): Observable<number[]> {
    const params = year ? `?year=${year}` : '';
    return this.http.get<number[]>(`${this.baseUrl}/income-sources/${sourceId}/yearly-simulation${params}`);
  }

  getTaxScenarios(sourceId: number, year?: number): Observable<{[key: string]: number[]}> {
    const params = year ? `?year=${year}` : '';
    return this.http.get<{[key: string]: number[]}>(`${this.baseUrl}/income-sources/${sourceId}/tax-scenarios${params}`);
  }

  getIncomesBySourceAndYear(sourceId: number, year: number): Observable<Income[]> {
    return this.http.get<Income[]>(`${this.baseUrl}/incomes/source/${sourceId}/year/${year}`);
  }

  // Tax Persons
  getAllTaxPersons(): Observable<TaxPerson[]> {
    return this.http.get<TaxPerson[]>(`${this.baseUrl}/tax-persons`);
  }

  getActiveTaxPersons(): Observable<TaxPerson[]> {
    return this.http.get<TaxPerson[]>(`${this.baseUrl}/tax-persons/active`);
  }

  getTaxPersonById(id: number): Observable<TaxPerson> {
    return this.http.get<TaxPerson>(`${this.baseUrl}/tax-persons/${id}`);
  }

  createTaxPerson(taxPerson: TaxPerson): Observable<TaxPerson> {
    return this.http.post<TaxPerson>(`${this.baseUrl}/tax-persons`, taxPerson);
  }

  updateTaxPerson(id: number, taxPerson: TaxPerson): Observable<TaxPerson> {
    return this.http.put<TaxPerson>(`${this.baseUrl}/tax-persons/${id}`, taxPerson);
  }

  deleteTaxPerson(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tax-persons/${id}`);
  }

  resetYearlyTracking(id: number, year: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/tax-persons/${id}/reset-yearly/${year}`, {});
  }

  getCostTypes(): Observable<CostTypeOption[]> {
    return this.http.get<CostTypeOption[]>(`${this.baseUrl}/tax-persons/cost-types`);
  }

  // Planned Purchases
  getAllPlannedPurchases(): Observable<PlannedPurchase[]> {
    return this.http.get<PlannedPurchase[]>(`${this.baseUrl}/planned-purchases`);
  }

  getPlannedPurchasesByYear(year: number): Observable<PlannedPurchase[]> {
    return this.http.get<PlannedPurchase[]>(`${this.baseUrl}/planned-purchases/year/${year}`);
  }

  getPlannedPurchasesByMonth(year: number, month: number): Observable<PlannedPurchase[]> {
    return this.http.get<PlannedPurchase[]>(`${this.baseUrl}/planned-purchases/year/${year}/month/${month}`);
  }

  createPlannedPurchase(purchase: PlannedPurchase): Observable<PlannedPurchase> {
    return this.http.post<PlannedPurchase>(`${this.baseUrl}/planned-purchases`, purchase);
  }

  updatePlannedPurchase(id: number, purchase: PlannedPurchase): Observable<PlannedPurchase> {
    return this.http.put<PlannedPurchase>(`${this.baseUrl}/planned-purchases/${id}`, purchase);
  }

  deletePlannedPurchase(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/planned-purchases/${id}`);
  }

  markPurchaseAsPurchased(id: number): Observable<PlannedPurchase> {
    return this.http.post<PlannedPurchase>(`${this.baseUrl}/planned-purchases/${id}/mark-purchased`, {});
  }

  // Savings Goals
  getAllSavingsGoals(): Observable<SavingsGoal[]> {
    return this.http.get<SavingsGoal[]>(`${this.baseUrl}/savings-goals`);
  }

  getActiveSavingsGoals(): Observable<SavingsGoal[]> {
    return this.http.get<SavingsGoal[]>(`${this.baseUrl}/savings-goals/active`);
  }

  createSavingsGoal(goal: SavingsGoal): Observable<SavingsGoal> {
    return this.http.post<SavingsGoal>(`${this.baseUrl}/savings-goals`, goal);
  }

  updateSavingsGoal(id: number, goal: SavingsGoal): Observable<SavingsGoal> {
    return this.http.put<SavingsGoal>(`${this.baseUrl}/savings-goals/${id}`, goal);
  }

  deleteSavingsGoal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/savings-goals/${id}`);
  }

  addSavingsContribution(id: number, amount: number): Observable<SavingsGoal> {
    return this.http.post<SavingsGoal>(`${this.baseUrl}/savings-goals/${id}/contribute`, { amount });
  }

  withdrawFromSavings(id: number, amount: number): Observable<SavingsGoal> {
    return this.http.post<SavingsGoal>(`${this.baseUrl}/savings-goals/${id}/withdraw`, { amount });
  }
}
