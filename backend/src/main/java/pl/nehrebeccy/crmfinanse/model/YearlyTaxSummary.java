package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "yearly_tax_summaries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tax_person_id", "year", "month"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyTaxSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_person_id", nullable = false)
    private TaxPerson taxPerson;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    // Wartości na koniec miesiąca
    @Column(name = "gross_income_ytd", precision = 15, scale = 2)
    private BigDecimal grossIncomeYTD = BigDecimal.ZERO;

    @Column(name = "taxable_income_ytd", precision = 15, scale = 2)
    private BigDecimal taxableIncomeYTD = BigDecimal.ZERO;

    @Column(name = "zus_em_rent_paid_ytd", precision = 15, scale = 2)
    private BigDecimal zusEmRentPaidYTD = BigDecimal.ZERO;

    @Column(name = "zus_health_paid_ytd", precision = 15, scale = 2)
    private BigDecimal zusHealthPaidYTD = BigDecimal.ZERO;

    @Column(name = "income_tax_paid_ytd", precision = 15, scale = 2)
    private BigDecimal incomeTaxPaidYTD = BigDecimal.ZERO;

    // Flagi
    @Column(name = "zus_limit_reached_this_month")
    private boolean zusLimitReachedThisMonth;

    @Column(name = "tax_bracket_changed_this_month")
    private boolean taxBracketChangedThisMonth;
}
