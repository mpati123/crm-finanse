package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // === Konfiguracja PIT ===
    @Column(name = "pit2_filed")
    private boolean pit2Filed;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type")
    private CostType costType = CostType.STANDARD_250;

    @Column(name = "joint_tax_return")
    private boolean jointTaxReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spouse_id")
    private TaxPerson spouse;

    // === Tracking roczny (automatyczny) ===
    @Column(name = "tax_year")
    private int taxYear;

    @Column(name = "cumulative_gross_income", precision = 15, scale = 2)
    private BigDecimal cumulativeGrossIncome = BigDecimal.ZERO;

    @Column(name = "cumulative_taxable_income", precision = 15, scale = 2)
    private BigDecimal cumulativeTaxableIncome = BigDecimal.ZERO;

    @Column(name = "cumulative_zus_paid", precision = 15, scale = 2)
    private BigDecimal cumulativeZusPaid = BigDecimal.ZERO;

    @Column(name = "zus_limit_reached")
    private boolean zusLimitReached;

    @Column(name = "second_tax_bracket")
    private boolean secondTaxBracket;

    // === Dla B2B - domyślne wartości ===
    @Enumerated(EnumType.STRING)
    @Column(name = "default_tax_form")
    private TaxForm defaultTaxForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_zus_type")
    private ZUSType defaultZusType;

    @Column(name = "default_vat_payer")
    private boolean defaultVatPayer;

    @Column(name = "active")
    private boolean active = true;
}
