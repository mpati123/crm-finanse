import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { Category } from '../../models/expense.model';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.scss'
})
export class CategoriesComponent implements OnInit {
  categories: Category[] = [];
  loading = false;
  showForm = false;
  editingCategory: Category | null = null;

  newCategory: Category = {
    name: '',
    color: '#3B82F6',
    type: 'EXPENSE'
  };

  constructor(private apiService: ApiService, private translate: TranslateService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.apiService.getAllCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.loading = false;
      }
    });
  }

  openForm(category?: Category): void {
    if (category) {
      this.editingCategory = category;
      this.newCategory = { ...category };
    } else {
      this.editingCategory = null;
      this.newCategory = {
        name: '',
        color: '#3B82F6',
        type: 'EXPENSE'
      };
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingCategory = null;
  }

  saveCategory(): void {
    if (this.editingCategory?.id) {
      this.apiService.updateCategory(this.editingCategory.id, this.newCategory).subscribe({
        next: () => {
          this.loadCategories();
          this.closeForm();
        },
        error: (err) => console.error('Error updating category:', err)
      });
    } else {
      this.apiService.createCategory(this.newCategory).subscribe({
        next: () => {
          this.loadCategories();
          this.closeForm();
        },
        error: (err) => console.error('Error creating category:', err)
      });
    }
  }

  deleteCategory(id: number): void {
    if (confirm(this.translate.instant('CATEGORIES.DELETE_CONFIRM'))) {
      this.apiService.deleteCategory(id).subscribe({
        next: () => this.loadCategories(),
        error: (err) => console.error('Error deleting category:', err)
      });
    }
  }

  getExpenseCategories(): Category[] {
    return this.categories.filter(c => c.type === 'EXPENSE');
  }

  getIncomeCategories(): Category[] {
    return this.categories.filter(c => c.type === 'INCOME');
  }

  getTypeLabel(type: string): string {
    return type === 'EXPENSE'
      ? this.translate.instant('CATEGORY_TYPES.EXPENSE')
      : this.translate.instant('CATEGORY_TYPES.INCOME');
  }
}
