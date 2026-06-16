package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.PlannedPurchaseDTO;
import pl.nehrebeccy.crmfinanse.model.Category;
import pl.nehrebeccy.crmfinanse.model.Expense;
import pl.nehrebeccy.crmfinanse.model.PlannedPurchase;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;
import pl.nehrebeccy.crmfinanse.repository.ExpenseRepository;
import pl.nehrebeccy.crmfinanse.repository.PlannedPurchaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannedPurchaseService {

    private final PlannedPurchaseRepository repository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public List<PlannedPurchaseDTO> getAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlannedPurchaseDTO> getByYearAndMonth(Integer year, Integer month) {
        return repository.findByPlannedYearAndPlannedMonth(year, month).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlannedPurchaseDTO> getByYear(Integer year) {
        return repository.findByPlannedYear(year).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlannedPurchaseDTO> getByStatus(PlannedPurchase.PurchaseStatus status) {
        return repository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PlannedPurchaseDTO getById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Planned purchase not found: " + id));
    }

    @Transactional
    public PlannedPurchaseDTO create(PlannedPurchaseDTO dto) {
        PlannedPurchase purchase = toEntity(dto);
        purchase = repository.save(purchase);

        // Create expense with PENDING status for planned purchase
        LocalDate expenseDate = LocalDate.of(purchase.getPlannedYear(), purchase.getPlannedMonth(), 1);
        Expense expense = Expense.builder()
                .name(purchase.getName())
                .amount(purchase.getAmount())
                .category(purchase.getCategory())
                .date(expenseDate)
                .status(Expense.PaymentStatus.PENDING)
                .notes("Zaplanowany zakup: " + purchase.getName())
                .recurring(false)
                .plannedPurchaseId(purchase.getId())
                .build();
        expense = expenseRepository.save(expense);

        // Link expense to purchase
        purchase.setExpenseId(expense.getId());
        return toDTO(repository.save(purchase));
    }

    @Transactional
    public PlannedPurchaseDTO update(Long id, PlannedPurchaseDTO dto) {
        PlannedPurchase purchase = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planned purchase not found: " + id));

        purchase.setName(dto.getName());
        purchase.setDescription(dto.getDescription());
        purchase.setAmount(dto.getAmount());
        purchase.setPlannedYear(dto.getPlannedYear());
        purchase.setPlannedMonth(dto.getPlannedMonth());
        purchase.setPriority(dto.getPriority());
        purchase.setStatus(dto.getStatus());

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
            purchase.setCategory(category);
        } else {
            purchase.setCategory(null);
        }

        // Update linked expense if exists
        if (purchase.getExpenseId() != null) {
            Expense expense = expenseRepository.findById(purchase.getExpenseId()).orElse(null);
            if (expense != null) {
                expense.setName(dto.getName());
                expense.setAmount(dto.getAmount());
                expense.setCategory(category);
                expense.setDate(LocalDate.of(dto.getPlannedYear(), dto.getPlannedMonth(), 1));
                expense.setNotes("Zaplanowany zakup: " + dto.getName());
                expenseRepository.save(expense);
            }
        }

        return toDTO(repository.save(purchase));
    }

    @Transactional
    public void delete(Long id) {
        PlannedPurchase purchase = repository.findById(id).orElse(null);
        if (purchase != null) {
            Long expenseId = purchase.getExpenseId();

            // First, unlink expense from planned purchase (remove planned_purchase_id from expense)
            if (expenseId != null) {
                Expense expense = expenseRepository.findById(expenseId).orElse(null);
                if (expense != null) {
                    expense.setPlannedPurchaseId(null);
                    expenseRepository.save(expense);
                    expenseRepository.flush(); // Force immediate DB update
                }
            }

            // Then remove the expense_id reference from planned purchase
            purchase.setExpenseId(null);
            repository.save(purchase);
            repository.flush(); // Force immediate DB update

            // Now delete the planned purchase
            repository.deleteById(id);

            // Finally delete the linked expense
            if (expenseId != null) {
                expenseRepository.deleteById(expenseId);
            }
        } else {
            repository.deleteById(id);
        }
    }

    @Transactional
    public PlannedPurchaseDTO linkExpense(Long id) {
        PlannedPurchase purchase = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planned purchase not found: " + id));

        // Only create expense if not already linked
        if (purchase.getExpenseId() == null) {
            LocalDate expenseDate = LocalDate.of(purchase.getPlannedYear(), purchase.getPlannedMonth(), 1);
            Expense expense = Expense.builder()
                    .name(purchase.getName())
                    .amount(purchase.getAmount())
                    .category(purchase.getCategory())
                    .date(expenseDate)
                    .status(purchase.getStatus() == PlannedPurchase.PurchaseStatus.PURCHASED
                            ? Expense.PaymentStatus.PAID
                            : Expense.PaymentStatus.PENDING)
                    .notes("Zaplanowany zakup: " + purchase.getName())
                    .recurring(false)
                    .plannedPurchaseId(purchase.getId())
                    .build();
            expense = expenseRepository.save(expense);

            purchase.setExpenseId(expense.getId());
            purchase = repository.save(purchase);
        }

        return toDTO(purchase);
    }

    @Transactional
    public PlannedPurchaseDTO markAsPurchased(Long id) {
        PlannedPurchase purchase = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planned purchase not found: " + id));

        // Update existing expense status to PAID
        if (purchase.getExpenseId() != null) {
            Expense expense = expenseRepository.findById(purchase.getExpenseId()).orElse(null);
            if (expense != null) {
                expense.setStatus(Expense.PaymentStatus.PAID);
                expenseRepository.save(expense);
            }
        }

        // Update purchase status
        purchase.setStatus(PlannedPurchase.PurchaseStatus.PURCHASED);

        return toDTO(repository.save(purchase));
    }

    private PlannedPurchaseDTO toDTO(PlannedPurchase purchase) {
        return PlannedPurchaseDTO.builder()
                .id(purchase.getId())
                .name(purchase.getName())
                .description(purchase.getDescription())
                .amount(purchase.getAmount())
                .categoryId(purchase.getCategory() != null ? purchase.getCategory().getId() : null)
                .categoryName(purchase.getCategory() != null ? purchase.getCategory().getName() : null)
                .plannedYear(purchase.getPlannedYear())
                .plannedMonth(purchase.getPlannedMonth())
                .priority(purchase.getPriority())
                .status(purchase.getStatus())
                .expenseId(purchase.getExpenseId())
                .createdAt(purchase.getCreatedAt())
                .updatedAt(purchase.getUpdatedAt())
                .build();
    }

    private PlannedPurchase toEntity(PlannedPurchaseDTO dto) {
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
        }

        return PlannedPurchase.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .category(category)
                .plannedYear(dto.getPlannedYear())
                .plannedMonth(dto.getPlannedMonth())
                .priority(dto.getPriority() != null ? dto.getPriority() : PlannedPurchase.Priority.MEDIUM)
                .status(dto.getStatus() != null ? dto.getStatus() : PlannedPurchase.PurchaseStatus.PLANNED)
                .expenseId(dto.getExpenseId())
                .build();
    }
}
