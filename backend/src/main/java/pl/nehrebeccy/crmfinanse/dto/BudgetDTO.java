package pl.nehrebeccy.crmfinanse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDTO {

    private Long id;

    @NotNull(message = "Rok jest wymagany")
    @Min(value = 2000, message = "Rok musi być >= 2000")
    private Integer year;

    @NotNull(message = "Miesiąc jest wymagany")
    @Min(value = 1, message = "Miesiąc musi być między 1 a 12")
    @Max(value = 12, message = "Miesiąc musi być między 1 a 12")
    private Integer month;

    private Long categoryId;
    private String categoryName;

    @NotNull(message = "Planowana kwota jest wymagana")
    @Positive(message = "Planowana kwota musi być dodatnia")
    private BigDecimal plannedAmount;

    private BigDecimal actualAmount;
    private BigDecimal difference;
    private Double percentageUsed;
}
