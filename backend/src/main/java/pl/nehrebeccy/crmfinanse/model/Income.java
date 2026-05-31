package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "incomes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income {

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

    @Column(nullable = false)
    private LocalDate date;

    private String notes;

    @Column(name = "is_recurring")
    private boolean recurring;

    @Column(name = "is_estimated")
    private Boolean estimated = false;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;
@Column(name = "actual_amount", precision = 10, scale = 2)    private BigDecimal actualAmount;

    @Column(name = "actual_hours")
    private Integer actualHours;

    @Column(name = "overtime_hours_100")
    private Integer overtimeHours100;

    @Column(name = "overtime_hours_150")
    private Integer overtimeHours150;

    @Column(name = "overtime_hours_200")
    private Integer overtimeHours200;

    @Column(name = "income_source_id")
    private Long incomeSourceId;
}
