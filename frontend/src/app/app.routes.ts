import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ExpensesComponent } from './components/expenses/expenses.component';
import { IncomesComponent } from './components/incomes/incomes.component';
import { CategoriesComponent } from './components/categories/categories.component';
import { BudgetComponent } from './components/budget/budget.component';
import { IncomeSourcesComponent } from './components/income-sources/income-sources.component';
import { ExpenseTemplatesComponent } from './components/expense-templates/expense-templates.component';
import { TaxPersonsComponent } from './components/tax-persons/tax-persons.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'expenses', component: ExpensesComponent },
  { path: 'incomes', component: IncomesComponent },
  { path: 'categories', component: CategoriesComponent },
  { path: 'budget', component: BudgetComponent },
  { path: 'income-sources', component: IncomeSourcesComponent },
  { path: 'expense-templates', component: ExpenseTemplatesComponent },
  { path: 'tax-persons', component: TaxPersonsComponent },
  { path: '**', redirectTo: '/dashboard' }
];
