package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.DashboardDTO;
import pl.nehrebeccy.crmfinanse.model.Expense;
import pl.nehrebeccy.crmfinanse.repository.ExpenseRepository;
import pl.nehrebeccy.crmfinanse.repository.IncomeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public DashboardDTO getDashboard(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        BigDecimal totalIncome = incomeRepository.sumAmountByDateBetween(startDate, endDate);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpenses = expenseRepository.sumAmountByDateBetween(startDate, endDate);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal pendingPayments = expenseRepository.findByStatus(Expense.PaymentStatus.PENDING).stream()
                .filter(e -> !e.getDate().isBefore(startDate) && !e.getDate().isAfter(endDate))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Roczne podsumowanie
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        BigDecimal yearlyIncome = incomeRepository.sumAmountByDateBetween(yearStart, yearEnd);
        if (yearlyIncome == null) yearlyIncome = BigDecimal.ZERO;

        BigDecimal yearlyExpenses = expenseRepository.sumAmountByDateBetween(yearStart, yearEnd);
        if (yearlyExpenses == null) yearlyExpenses = BigDecimal.ZERO;

        List<DashboardDTO.CategorySummary> expensesByCategory = getExpensesByCategory(startDate, endDate, totalExpenses);
        List<DashboardDTO.MonthlyTrend> monthlyTrends = getMonthlyTrends();

        return DashboardDTO.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .balance(totalIncome.subtract(totalExpenses))
                .pendingPayments(pendingPayments)
                .yearlyIncome(yearlyIncome)
                .yearlyExpenses(yearlyExpenses)
                .yearlyBalance(yearlyIncome.subtract(yearlyExpenses))
                .expensesByCategory(expensesByCategory)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    private List<DashboardDTO.CategorySummary> getExpensesByCategory(LocalDate startDate, LocalDate endDate, BigDecimal totalExpenses) {
        List<Object[]> results = expenseRepository.sumAmountByCategoryAndDateBetween(startDate, endDate);

        return results.stream().map(row -> {
            String categoryName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(totalExpenses, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            return DashboardDTO.CategorySummary.builder()
                    .categoryName(categoryName != null ? categoryName : "Bez kategorii")
                    .amount(amount)
                    .percentage(percentage)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<DashboardDTO.MonthlyTrend> getMonthlyTrends() {
        List<Object[]> expensesByMonth = expenseRepository.sumAmountByMonth();
        List<Object[]> incomesByMonth = incomeRepository.sumAmountByMonth();

        List<DashboardDTO.MonthlyTrend> trends = new ArrayList<>();

        for (Object[] row : expensesByMonth) {
            Integer year = (Integer) row[0];
            Integer month = (Integer) row[1];
            BigDecimal expenses = (BigDecimal) row[2];

            BigDecimal income = BigDecimal.ZERO;
            for (Object[] incomeRow : incomesByMonth) {
                if (incomeRow[0].equals(year) && incomeRow[1].equals(month)) {
                    income = (BigDecimal) incomeRow[2];
                    break;
                }
            }

            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("pl", "PL"));

            trends.add(DashboardDTO.MonthlyTrend.builder()
                    .year(year)
                    .month(month)
                    .monthName(monthName)
                    .income(income)
                    .expenses(expenses)
                    .balance(income.subtract(expenses))
                    .build());
        }

        return trends;
    }
}
