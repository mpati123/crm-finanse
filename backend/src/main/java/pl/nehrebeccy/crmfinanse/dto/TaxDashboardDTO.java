package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxDashboardDTO {

    private int year;
    private int month;

    // VAT Summary
    private VatSummary vatSummary;

    // PIT Summary
    private PitSummary pitSummary;

    // Terminy płatności
    private List<TaxDeadline> upcomingDeadlines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VatSummary {
        private BigDecimal inputVatTotal;   // VAT naliczony
        private BigDecimal outputVatTotal;  // VAT należny
        private BigDecimal vatToPay;        // do zapłaty
        private BigDecimal vatToReturn;     // do zwrotu
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PitSummary {
        private BigDecimal grossIncome;     // przychód brutto
        private BigDecimal totalCosts;      // koszty
        private BigDecimal taxBase;         // podstawa opodatkowania
        private BigDecimal taxToPay;        // podatek do zapłaty
        private BigDecimal zusContributions; // składki ZUS
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaxDeadline {
        private String type;                // VAT, PIT, ZUS
        private LocalDate deadline;         // termin płatności
        private BigDecimal amount;          // kwota do zapłaty
        private boolean overdue;            // czy po terminie
    }
}
