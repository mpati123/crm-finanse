package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.nehrebeccy.crmfinanse.model.SavingsGoal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private BigDecimal monthlyContribution;
    private SavingsGoal.Priority priority;
    private SavingsGoal.GoalStatus status;
    private String icon;
    private String color;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private BigDecimal remainingAmount;
    private Double percentageComplete;
    private Integer monthsToGoal;
}
