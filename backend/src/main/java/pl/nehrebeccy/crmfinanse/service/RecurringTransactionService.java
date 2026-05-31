package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.ExpenseDTO;
import pl.nehrebeccy.crmfinanse.dto.IncomeDTO;
import pl.nehrebeccy.crmfinanse.dto.IncomeSourceDTO;
import pl.nehrebeccy.crmfinanse.model.*;
import pl.nehrebeccy.crmfinanse.repository.ExpenseRepository;
import pl.nehrebeccy.crmfinanse.repository.ExpenseTemplateRepository;
import pl.nehrebeccy.crmfinanse.repository.IncomeRepository;
import pl.nehrebeccy.crmfinanse.repository.IncomeSourceRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecurringTransactionService {

    private final IncomeSourceRepository incomeSourceRepository;
    private final ExpenseTemplateRepository expenseTemplateRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final IncomeCalculationService calculationService;

    public List<IncomeDTO> generateIncomesForMonth(int year, int month) {
        List<IncomeDTO> generatedIncomes = new ArrayList<>();
        LocalDate monthDate = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthDate.plusMonths(1).minusDays(1);

        List<IncomeSource> activeSources = incomeSourceRepository.findByActiveTrue();

        for (IncomeSource source : activeSources) {
            // Sprawdź czy źródło jest aktywne w tym miesiącu
            if (!isSourceActiveInPeriod(source, monthDate, monthEnd)) {
                continue;
            }

            // Sprawdź czy przychód już istnieje dla tego źródła w tym miesiącu
            if (incomeExistsForSourceInMonth(source, year, month)) {
                log.info("Przychód już istnieje dla źródła {} w {}/{}", source.getName(), year, month);
                continue;
            }

            // Oblicz datę płatności
            int paymentDay = source.getPaymentDayOfMonth() != null ?
                    source.getPaymentDayOfMonth() : 10;
            LocalDate paymentDate = LocalDate.of(year, month,
                    Math.min(paymentDay, monthEnd.getDayOfMonth()));

            // Czy to przyszły przychód?
            boolean isFuture = paymentDate.isAfter(LocalDate.now());

            // Oblicz netto
            IncomeSourceDTO sourceDTO = toIncomeSourceDTO(source);
            calculationService.calculateNetIncome(sourceDTO);

            // Ustaw actualHours na podstawie defaultHoursPerMonth dla stawki godzinowej
            Integer actualHours = null;
            if (source.getRateType() == RateType.HOURLY && source.getDefaultHoursPerMonth() != null) {
                actualHours = source.getDefaultHoursPerMonth();
            }

            // Utwórz przychód
            IncomeDTO incomeDTO = IncomeDTO.builder()
                    .name(source.getName())
                    .amount(source.getAmount())
                    .categoryId(source.getCategory() != null ? source.getCategory().getId() : null)
                    .date(paymentDate)
                    .notes(buildIncomeNotes(source))
                    .recurring(true)
                    .estimated(isFuture)
                    .netAmount(sourceDTO.getNetAmount())
                    .actualHours(actualHours)
                    .overtimeHours100(0)
                    .overtimeHours150(0)
                    .overtimeHours200(0)
                    .incomeSourceId(source.getId())
                    .build();

            IncomeDTO created = incomeService.createIncome(incomeDTO);
            generatedIncomes.add(created);
            log.info("Wygenerowano przychód: {} na {} PLN (netto: {} PLN)",
                    created.getName(), created.getAmount(), created.getNetAmount());
        }

        return generatedIncomes;
    }

    public List<ExpenseDTO> generateExpensesForMonth(int year, int month) {
        List<ExpenseDTO> generatedExpenses = new ArrayList<>();
        LocalDate monthDate = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthDate.plusMonths(1).minusDays(1);

        List<ExpenseTemplate> activeTemplates = expenseTemplateRepository.findActiveTemplatesForDate(monthDate);

        for (ExpenseTemplate template : activeTemplates) {
            // Sprawdź częstotliwość
            if (!shouldGenerateExpense(template, year, month)) {
                continue;
            }

            // Sprawdź czy wydatek już istnieje
            if (expenseExistsForTemplateInMonth(template, year, month)) {
                log.info("Wydatek już istnieje dla szablonu {} w {}/{}", template.getName(), year, month);
                continue;
            }

            // Oblicz datę płatności
            int paymentDay = template.getDayOfMonth() != null ?
                    template.getDayOfMonth() : 1;
            LocalDate paymentDate = LocalDate.of(year, month,
                    Math.min(paymentDay, monthEnd.getDayOfMonth()));

            // Czy to przyszły wydatek?
            boolean isFuture = paymentDate.isAfter(LocalDate.now());

            // Określ status
            String status;
            if (template.isAutoPay()) {
                status = isFuture ? "PENDING" : "PAID";
            } else {
                status = "PENDING";
            }

            // Utwórz wydatek
            ExpenseDTO expenseDTO = ExpenseDTO.builder()
                    .name(template.getName())
                    .amount(template.getAmount())
                    .categoryId(template.getCategory() != null ? template.getCategory().getId() : null)
                    .date(paymentDate)
                    .status(status)
                    .notes(template.getNotes())
                    .recurring(true)
                    .build();

            ExpenseDTO created = expenseService.createExpense(expenseDTO);
            generatedExpenses.add(created);
            log.info("Wygenerowano wydatek: {} na {} PLN", created.getName(), created.getAmount());
        }

        return generatedExpenses;
    }

    public GenerationResult generateAllForMonth(int year, int month) {
        List<IncomeDTO> incomes = generateIncomesForMonth(year, month);
        List<ExpenseDTO> expenses = generateExpensesForMonth(year, month);
        return new GenerationResult(incomes, expenses);
    }

    private boolean isSourceActiveInPeriod(IncomeSource source, LocalDate start, LocalDate end) {
        if (source.getStartDate() != null && source.getStartDate().isAfter(end)) {
            return false;
        }
        if (source.getEndDate() != null && source.getEndDate().isBefore(start)) {
            return false;
        }
        return true;
    }

    private boolean incomeExistsForSourceInMonth(IncomeSource source, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Income> existingIncomes = incomeRepository.findByDateBetween(startDate, endDate);
        return existingIncomes.stream()
                .anyMatch(income -> income.getName().contains(source.getName()) && income.isRecurring());
    }

    private boolean expenseExistsForTemplateInMonth(ExpenseTemplate template, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Expense> existingExpenses = expenseRepository.findByDateBetween(startDate, endDate);
        return existingExpenses.stream()
                .anyMatch(expense -> expense.getName().equals(template.getName()) && expense.isRecurring());
    }

    private boolean shouldGenerateExpense(ExpenseTemplate template, int year, int month) {
        switch (template.getFrequency()) {
            case MONTHLY:
                return true;
            case QUARTERLY:
                return month == 1 || month == 4 || month == 7 || month == 10;
            case YEARLY:
                return month == 1;
            case ONE_TIME:
                return false;
            default:
                return false;
        }
    }

    private String buildIncomeNotes(IncomeSource source) {
        StringBuilder notes = new StringBuilder();
        notes.append("Wygenerowano automatycznie z: ").append(source.getName());

        if (source.getIncomeType() != null) {
            notes.append(" (").append(source.getIncomeType().getDisplayName()).append(")");
        }

        return notes.toString();
    }

    private IncomeSourceDTO toIncomeSourceDTO(IncomeSource entity) {
        IncomeSourceDTO.IncomeSourceDTOBuilder builder = IncomeSourceDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .personName(entity.getPersonName())
                .incomeType(entity.getIncomeType())
                .amount(entity.getAmount())
                .amountType(entity.getAmountType())
                .rateType(entity.getRateType())
                .hourlyRate(entity.getHourlyRate())
                .defaultHoursPerMonth(entity.getDefaultHoursPerMonth())
                .employmentFraction(entity.getEmploymentFraction())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .paymentDayOfMonth(entity.getPaymentDayOfMonth())
                .active(entity.isActive())
                .notes(entity.getNotes());

        if (entity.getCategory() != null) {
            builder.categoryId(entity.getCategory().getId())
                   .categoryName(entity.getCategory().getName());
        }

        // TODO: dodać mapowanie B2B i UoP config

        return builder.build();
    }

    public record GenerationResult(List<IncomeDTO> incomes, List<ExpenseDTO> expenses) {}
}
