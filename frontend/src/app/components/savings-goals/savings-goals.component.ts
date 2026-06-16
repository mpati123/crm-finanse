import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApiService } from '../../services/api.service';
import { SavingsGoal, GOAL_PRIORITY_OPTIONS, GOAL_STATUS_OPTIONS, GOAL_ICONS, GoalPriority, GoalStatus } from '../../models/savings-goal.model';

@Component({
  selector: 'app-savings-goals',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './savings-goals.component.html',
  styleUrl: './savings-goals.component.scss'
})
export class SavingsGoalsComponent implements OnInit {
  goals: SavingsGoal[] = [];
  filteredGoals: SavingsGoal[] = [];
  loading = false;
  showForm = false;
  showContributionModal = false;
  showWithdrawModal = false;
  editingGoal: SavingsGoal | null = null;
  selectedGoalForAction: SavingsGoal | null = null;
  contributionAmount = 0;
  withdrawAmount = 0;

  priorityOptions = GOAL_PRIORITY_OPTIONS;
  statusOptions = GOAL_STATUS_OPTIONS;
  iconOptions = GOAL_ICONS;

  // Filtering
  filterText = '';
  filterStatus: GoalStatus | null = null;
  filterPriority: GoalPriority | null = null;

  // Sorting
  sortColumn: 'name' | 'targetAmount' | 'percentageComplete' | 'priority' | 'targetDate' = 'priority';
  sortDirection: 'asc' | 'desc' = 'asc';

  newGoal: SavingsGoal = {
    name: '',
    targetAmount: 0,
    currentAmount: 0,
    priority: 'MEDIUM',
    status: 'ACTIVE',
    icon: 'other',
    color: '#8b5cf6'
  };

  colorOptions = [
    '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6',
    '#6366f1', '#ef4444', '#14b8a6', '#f97316', '#84cc16'
  ];

  constructor(private apiService: ApiService, private translate: TranslateService) {}

  ngOnInit(): void {
    this.loadGoals();
  }

  loadGoals(): void {
    this.loading = true;
    this.apiService.getAllSavingsGoals().subscribe({
      next: (data) => {
        this.goals = data;
        this.applyFiltersAndSort();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading savings goals:', err);
        this.loading = false;
      }
    });
  }

  // === Filtering ===
  applyFiltersAndSort(): void {
    let result = [...this.goals];

    if (this.filterText) {
      const searchLower = this.filterText.toLowerCase();
      result = result.filter(g =>
        g.name.toLowerCase().includes(searchLower) ||
        (g.description && g.description.toLowerCase().includes(searchLower))
      );
    }

    if (this.filterStatus) {
      result = result.filter(g => g.status === this.filterStatus);
    }

    if (this.filterPriority) {
      result = result.filter(g => g.priority === this.filterPriority);
    }

    result.sort((a, b) => {
      let valueA: any;
      let valueB: any;

      switch (this.sortColumn) {
        case 'name':
          valueA = a.name.toLowerCase();
          valueB = b.name.toLowerCase();
          break;
        case 'targetAmount':
          valueA = a.targetAmount;
          valueB = b.targetAmount;
          break;
        case 'percentageComplete':
          valueA = a.percentageComplete || 0;
          valueB = b.percentageComplete || 0;
          break;
        case 'priority':
          const priorityOrder = { 'HIGH': 0, 'MEDIUM': 1, 'LOW': 2 };
          valueA = priorityOrder[a.priority];
          valueB = priorityOrder[b.priority];
          break;
        case 'targetDate':
          valueA = a.targetDate ? new Date(a.targetDate).getTime() : Number.MAX_VALUE;
          valueB = b.targetDate ? new Date(b.targetDate).getTime() : Number.MAX_VALUE;
          break;
        default:
          return 0;
      }

      if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredGoals = result;
  }

  onFilterChange(): void {
    this.applyFiltersAndSort();
  }

  clearFilters(): void {
    this.filterText = '';
    this.filterStatus = null;
    this.filterPriority = null;
    this.applyFiltersAndSort();
  }

  // === Sorting ===
  sort(column: 'name' | 'targetAmount' | 'percentageComplete' | 'priority' | 'targetDate'): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applyFiltersAndSort();
  }

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) return '↕️';
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  openForm(goal?: SavingsGoal): void {
    if (goal) {
      this.editingGoal = goal;
      this.newGoal = { ...goal };
    } else {
      this.editingGoal = null;
      this.newGoal = {
        name: '',
        targetAmount: 0,
        currentAmount: 0,
        priority: 'MEDIUM',
        status: 'ACTIVE',
        icon: 'other',
        color: '#8b5cf6'
      };
    }
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingGoal = null;
  }

  saveGoal(): void {
    if (this.editingGoal?.id) {
      this.apiService.updateSavingsGoal(this.editingGoal.id, this.newGoal).subscribe({
        next: () => {
          this.loadGoals();
          this.closeForm();
        },
        error: (err) => console.error('Error updating goal:', err)
      });
    } else {
      this.apiService.createSavingsGoal(this.newGoal).subscribe({
        next: () => {
          this.loadGoals();
          this.closeForm();
        },
        error: (err) => console.error('Error creating goal:', err)
      });
    }
  }

  deleteGoal(id: number): void {
    if (confirm('Czy na pewno chcesz usunąć ten cel oszczędnościowy?')) {
      this.apiService.deleteSavingsGoal(id).subscribe({
        next: () => this.loadGoals(),
        error: (err) => console.error('Error deleting goal:', err)
      });
    }
  }

  openContributionModal(goal: SavingsGoal): void {
    this.selectedGoalForAction = goal;
    this.contributionAmount = goal.monthlyContribution || 0;
    this.showContributionModal = true;
  }

  closeContributionModal(): void {
    this.showContributionModal = false;
    this.selectedGoalForAction = null;
    this.contributionAmount = 0;
  }

  addContribution(): void {
    if (this.selectedGoalForAction?.id && this.contributionAmount > 0) {
      this.apiService.addSavingsContribution(this.selectedGoalForAction.id, this.contributionAmount).subscribe({
        next: () => {
          this.loadGoals();
          this.closeContributionModal();
        },
        error: (err) => console.error('Error adding contribution:', err)
      });
    }
  }

  openWithdrawModal(goal: SavingsGoal): void {
    this.selectedGoalForAction = goal;
    this.withdrawAmount = 0;
    this.showWithdrawModal = true;
  }

  closeWithdrawModal(): void {
    this.showWithdrawModal = false;
    this.selectedGoalForAction = null;
    this.withdrawAmount = 0;
  }

  withdrawFromGoal(): void {
    if (this.selectedGoalForAction?.id && this.withdrawAmount > 0) {
      this.apiService.withdrawFromSavings(this.selectedGoalForAction.id, this.withdrawAmount).subscribe({
        next: () => {
          this.loadGoals();
          this.closeWithdrawModal();
        },
        error: (err) => console.error('Error withdrawing:', err)
      });
    }
  }

  getTotalSaved(): number {
    return this.goals.reduce((sum, g) => sum + g.currentAmount, 0);
  }

  getTotalTarget(): number {
    return this.goals.filter(g => g.status === 'ACTIVE').reduce((sum, g) => sum + g.targetAmount, 0);
  }

  getActiveGoalsCount(): number {
    return this.goals.filter(g => g.status === 'ACTIVE').length;
  }

  getCompletedGoalsCount(): number {
    return this.goals.filter(g => g.status === 'COMPLETED').length;
  }

  getPriorityLabel(priority: GoalPriority): string {
    return this.priorityOptions.find(p => p.value === priority)?.label || priority;
  }

  getStatusLabel(status: GoalStatus): string {
    return this.statusOptions.find(s => s.value === status)?.label || status;
  }

  getIconEmoji(icon?: string): string {
    return this.iconOptions.find(i => i.value === icon)?.emoji || '🎯';
  }

  getPriorityClass(priority: GoalPriority): string {
    return `priority-${priority.toLowerCase()}`;
  }

  getStatusClass(status: GoalStatus): string {
    return `status-${status.toLowerCase()}`;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(amount);
  }

  formatDate(date?: string): string {
    if (!date) return '-';
    return new Date(date).toLocaleDateString('pl-PL');
  }

  getProgressBarWidth(goal: SavingsGoal): string {
    const percentage = goal.percentageComplete || (goal.currentAmount / goal.targetAmount * 100);
    return Math.min(percentage, 100) + '%';
  }

  getProgressColor(goal: SavingsGoal): string {
    const percentage = goal.percentageComplete || (goal.currentAmount / goal.targetAmount * 100);
    if (percentage >= 100) return '#10b981';
    if (percentage >= 75) return '#3b82f6';
    if (percentage >= 50) return '#f59e0b';
    return '#ef4444';
  }
}
