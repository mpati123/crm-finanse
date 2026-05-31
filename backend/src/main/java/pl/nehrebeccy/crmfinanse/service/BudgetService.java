package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.BudgetDTO;
import pl.nehrebeccy.crmfinanse.model.Budget;
import pl.nehrebeccy.crmfinanse.model.Category;
import pl.nehrebeccy.crmfinanse.repository.BudgetRepository;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;
import pl.nehrebeccy.crmfinanse.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public List<BudgetDTO> getBudgetsByMonth(int year, int month) {
        return budgetRepository.findByYearAndMonth(year, month).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BudgetDTO getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Budżet nie znaleziony: " + id));
    }

    public BudgetDTO createBudget(BudgetDTO dto) {
        Budget budget = toEntity(dto);
        budget = budgetRepository.save(budget);
        return toDTO(budget);
    }

    public BudgetDTO updateBudget(Long id, BudgetDTO dto) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budżet nie znaleziony: " + id));

        budget.setYear(dto.getYear());
        budget.setMonth(dto.getMonth());
        budget.setPlannedAmount(dto.getPlannedAmount());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            budget.setCategory(category);
        }

        return toDTO(budgetRepository.save(budget));
    }

    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    public List<BudgetDTO> getBudgetsWithActuals(int year, int month) {
        List<Budget> budgets = budgetRepository.findByYearAndMonth(year, month);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return budgets.stream().map(budget -> {
            BudgetDTO dto = toDTO(budget);

            if (budget.getCategory() != null) {
                BigDecimal actual = expenseRepository.findByCategoryId(budget.getCategory().getId()).stream()
                        .filter(e -> !e.getDate().isBefore(startDate) && !e.getDate().isAfter(endDate))
                        .map(e -> e.getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                dto.setActualAmount(actual);
                dto.setDifference(budget.getPlannedAmount().subtract(actual));

                if (budget.getPlannedAmount().compareTo(BigDecimal.ZERO) > 0) {
                    double percentage = actual.divide(budget.getPlannedAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    dto.setPercentageUsed(percentage);
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    private BudgetDTO toDTO(Budget budget) {
        return BudgetDTO.builder()
                .id(budget.getId())
                .year(budget.getYear())
                .month(budget.getMonth())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : null)
                .plannedAmount(budget.getPlannedAmount())
                .actualAmount(budget.getActualAmount())
                .build();
    }

    private Budget toEntity(BudgetDTO dto) {
        Budget budget = Budget.builder()
                .year(dto.getYear())
                .month(dto.getMonth())
                .plannedAmount(dto.getPlannedAmount())
                .build();

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            budget.setCategory(category);
        }

        return budget;
    }
}
