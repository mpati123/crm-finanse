package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expense_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private ExpenseFrequency frequency;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "notes")
    private String notes;

    @Column(name = "auto_pay")
    private boolean autoPay = false;
}
