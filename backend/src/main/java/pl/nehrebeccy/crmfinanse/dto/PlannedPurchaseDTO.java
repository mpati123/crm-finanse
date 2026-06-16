package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.nehrebeccy.crmfinanse.model.PlannedPurchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedPurchaseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private Integer plannedYear;
    private Integer plannedMonth;
    private PlannedPurchase.Priority priority;
    private PlannedPurchase.PurchaseStatus status;
    private Long expenseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
