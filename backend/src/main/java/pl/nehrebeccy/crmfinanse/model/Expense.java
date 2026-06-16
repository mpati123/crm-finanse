package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "vat_rate")
    private Integer vatRate;

    @Column(name = "vat_amount", precision = 10, scale = 2)
    private BigDecimal vatAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String notes;

    @Column(name = "is_recurring")
    private boolean recurring;

    @Column(name = "expense_template_id")
    private Long expenseTemplateId;

    @Column(name = "planned_purchase_id")
    private Long plannedPurchaseId;

    public enum PaymentStatus {
        PAID,       // opłacone (t)
        PENDING,    // do zapłaty (n)
        REMAINING   // zostało (zost)
    }
}
