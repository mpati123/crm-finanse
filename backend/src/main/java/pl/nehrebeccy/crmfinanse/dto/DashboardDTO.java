package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private BigDecimal pendingPayments;

    // Roczne podsumowanie
    private BigDecimal yearlyIncome;
    private BigDecimal yearlyExpenses;
    private BigDecimal yearlyBalance;

    private List<CategorySummary> expensesByCategory;
    private List<MonthlyTrend> monthlyTrends;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySummary {
        private String categoryName;
        private BigDecimal amount;
        private Double percentage;
        private String color;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyTrend {
        private Integer year;
        private Integer month;
        private String monthName;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal balance;
    }
}
