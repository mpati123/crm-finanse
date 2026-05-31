package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PitSettlementDTO {

    private int year;
    private int month;

    // Przychody
    private BigDecimal grossIncome;           // przychód brutto
    private BigDecimal netIncome;             // dochód netto (po kosztach)
    private List<IncomeBreakdown> incomeBySource;

    // Koszty
    private BigDecimal totalCosts;            // suma kosztów
    private BigDecimal deductibleCosts;       // koszty uzyskania przychodu
    private List<CostBreakdown> costsByCategory;

    // Składki ZUS
    private BigDecimal zusContributions;      // składki ZUS
    private BigDecimal zusHealthDeductible;   // składka zdrowotna do odliczenia

    // Podatek
    private BigDecimal taxBase;               // podstawa opodatkowania
    private BigDecimal taxRate;               // stawka podatkowa (12%, 32%)
    private BigDecimal taxAmount;             // kwota podatku
    private BigDecimal taxToPay;              // podatek do zapłaty po odliczeniach

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeBreakdown {
        private String sourceName;
        private String incomeType;
        private BigDecimal grossAmount;
        private BigDecimal netAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CostBreakdown {
        private String categoryName;
        private BigDecimal amount;
        private boolean deductible;
    }
}
