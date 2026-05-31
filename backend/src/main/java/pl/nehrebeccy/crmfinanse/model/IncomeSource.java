package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "income_sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "person_name")
    private String personName;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_type", nullable = false)
    private IncomeType incomeType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "amount_type", nullable = false)
    private AmountType amountType = AmountType.GROSS;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false)
    private RateType rateType = RateType.MONTHLY;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "default_hours_per_month")
    private Integer defaultHoursPerMonth;

    @Column(name = "employment_fraction", precision = 5, scale = 2)
    private BigDecimal employmentFraction;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "payment_day_of_month")
    private Integer paymentDayOfMonth;

    @Column(name = "active")
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "b2b_config_id")
    private B2BConfig b2bConfig;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "uop_config_id")
    private UoPConfig uopConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_person_id")
    private TaxPerson taxPerson;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "umowa_zlecenie_config_id")
    private UmowaZlecenieConfig umowaZlecenieConfig;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "umowa_o_dzielo_config_id")
    private UmowaODzieloConfig umowaODzieloConfig;

    @Column(name = "notes")
    private String notes;
}
