package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "b2b_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2BConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_form", nullable = false)
    private TaxForm taxForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "zus_type", nullable = false)
    private ZUSType zusType;

    @Column(name = "vat_payer")
    private boolean vatPayer;

    @Column(name = "vat_rate")
    private Integer vatRate;

    @Column(name = "zus_amount", precision = 10, scale = 2)
    private BigDecimal zusAmount;

    @Column(name = "health_insurance", precision = 10, scale = 2)
    private BigDecimal healthInsurance;

    @Column(name = "income_tax_advance", precision = 10, scale = 2)
    private BigDecimal incomeTaxAdvance;

    @Column(name = "ryczalt_rate")
    private BigDecimal ryczaltRate;
}
