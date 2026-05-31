package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.ExpenseDTO;
import pl.nehrebeccy.crmfinanse.model.Category;
import pl.nehrebeccy.crmfinanse.model.Expense;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;
import pl.nehrebeccy.crmfinanse.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return expenseRepository.findByDateBetween(startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByStatus(String status) {
        Expense.PaymentStatus paymentStatus = Expense.PaymentStatus.valueOf(status.toUpperCase());
        return expenseRepository.findByStatus(paymentStatus).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Wydatek nie znaleziony: " + id));
    }

    public ExpenseDTO createExpense(ExpenseDTO dto) {
        Expense expense = toEntity(dto);
        expense = expenseRepository.save(expense);
        return toDTO(expense);
    }

    public ExpenseDTO updateExpense(Long id, ExpenseDTO dto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wydatek nie znaleziony: " + id));

        expense.setName(dto.getName());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setStatus(Expense.PaymentStatus.valueOf(dto.getStatus().toUpperCase()));
        expense.setNotes(dto.getNotes());
        expense.setRecurring(dto.isRecurring());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            expense.setCategory(category);
        }

        return toDTO(expenseRepository.save(expense));
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    public BigDecimal getTotalExpensesByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        BigDecimal total = expenseRepository.sumAmountByDateBetween(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    private ExpenseDTO toDTO(Expense expense) {
        return ExpenseDTO.builder()
                .id(expense.getId())
                .name(expense.getName())
                .amount(expense.getAmount())
                .categoryId(expense.getCategory() != null ? expense.getCategory().getId() : null)
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .date(expense.getDate())
                .status(expense.getStatus().name())
                .notes(expense.getNotes())
                .recurring(expense.isRecurring())
                .expenseTemplateId(expense.getExpenseTemplateId())
                .build();
    }

    private Expense toEntity(ExpenseDTO dto) {
        Expense expense = Expense.builder()
                .name(dto.getName())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .status(Expense.PaymentStatus.valueOf(dto.getStatus().toUpperCase()))
                .notes(dto.getNotes())
                .recurring(dto.isRecurring())
                .expenseTemplateId(dto.getExpenseTemplateId())
                .build();

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            expense.setCategory(category);
        }

        return expense;
    }
}
