import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { Category } from '../../models/expense.model';
import { ExpenseTemplate, ExpenseFrequency, FrequencyOption } from '../../models/expense-template.model';

@Component({
  selector: 'app-expense-templates',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './expense-templates.component.html',
  styleUrl: './expense-templates.component.scss'
})
export class ExpenseTemplatesComponent implements OnInit {
  templates: ExpenseTemplate[] = [];
  categories: Category[] = [];
  frequencies: FrequencyOption[] = [];
  loading = false;
  showForm = false;
  editingTemplate: ExpenseTemplate | null = null;

  newTemplate: ExpenseTemplate = this.getEmptyTemplate();

  constructor(private apiService: ApiService, private translate: TranslateService) {}

  ngOnInit(): void {
    this.loadTemplates();
    this.loadCategories();
    this.loadFrequencies();
  }

  getEmptyTemplate(): ExpenseTemplate {
    return {
      name: '',
      amount: 0,
      frequency: 'MONTHLY',
      active: true,
      autoPay: false,
      dayOfMonth: 1
    };
  }

  loadTemplates(): void {
    this.loading = true;
    this.apiService.getAllExpenseTemplates().subscribe({
      next: (data) => {
        this.templates = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading expense templates:', err);
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.apiService.getCategoriesByType('EXPENSE').subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error('Error loading categories:', err)
    });
  }

  loadFrequencies(): void {
    this.apiService.getExpenseFrequencies().subscribe({
      next: (data) => this.frequencies = data,
      error: (err) => console.error('Error loading frequencies:', err)
    });
  }

  openForm(template?: ExpenseTemplate): void {
    if (template) {
      this.editingTemplate = template;
      this.newTemplate = { ...template };
    } else {
      this.editingTemplate = null;
      this.newTemplate = this.getEmptyTemplate();
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingTemplate = null;
  }

  saveTemplate(): void {
    if (this.editingTemplate?.id) {
      this.apiService.updateExpenseTemplate(this.editingTemplate.id, this.newTemplate).subscribe({
        next: () => {
          this.loadTemplates();
          this.closeForm();
        },
        error: (err) => console.error('Error updating expense template:', err)
      });
    } else {
      this.apiService.createExpenseTemplate(this.newTemplate).subscribe({
        next: () => {
          this.loadTemplates();
          this.closeForm();
        },
        error: (err) => console.error('Error creating expense template:', err)
      });
    }
  }

  deleteTemplate(id: number): void {
    if (confirm(this.translate.instant('EXPENSE_TEMPLATES.DELETE_CONFIRM'))) {
      this.apiService.deleteExpenseTemplate(id).subscribe({
        next: () => this.loadTemplates(),
        error: (err) => console.error('Error deleting expense template:', err)
      });
    }
  }

  toggleActive(template: ExpenseTemplate): void {
    const updated = { ...template, active: !template.active };
    this.apiService.updateExpenseTemplate(template.id!, updated).subscribe({
      next: () => this.loadTemplates(),
      error: (err) => console.error('Error toggling active status:', err)
    });
  }

  getFrequencyLabel(freq: ExpenseFrequency): string {
    const found = this.frequencies.find(f => f.value === freq);
    return found ? found.label : freq;
  }

  getActiveCount(): number {
    return this.templates.filter(t => t.active).length;
  }

  getTotalMonthly(): number {
    return this.templates
      .filter(t => t.active)
      .reduce((sum, t) => {
        let monthly = t.amount;
        switch (t.frequency) {
          case 'QUARTERLY':
            monthly = t.amount / 3;
            break;
          case 'YEARLY':
            monthly = t.amount / 12;
            break;
          case 'ONE_TIME':
            monthly = 0;
            break;
        }
        return sum + monthly;
      }, 0);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }

  generateExpenses(): void {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;

    this.apiService.generateExpensesForMonth(year, month).subscribe({
      next: (result) => {
        alert(this.translate.instant('EXPENSES.GENERATED_SUCCESS', { count: result.length }));
      },
      error: (err) => console.error('Error generating expenses:', err)
    });
  }
}
