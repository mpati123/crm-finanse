package pl.nehrebeccy.crmfinanse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.nehrebeccy.crmfinanse.model.AmountType;
import pl.nehrebeccy.crmfinanse.model.IncomeType;
import pl.nehrebeccy.crmfinanse.model.RateType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeSourceDTO {

    private Long id;

    @NotBlank(message = "Nazwa jest wymagana")
    private String name;

    private String personName;

    @NotNull(message = "Typ przychodu jest wymagany")
    private IncomeType incomeType;

    @NotNull(message = "Kwota jest wymagana")
    @Positive(message = "Kwota musi być dodatnia")
    private BigDecimal amount;

    private AmountType amountType;

    private RateType rateType;
    private BigDecimal hourlyRate;
    private Integer defaultHoursPerMonth;
    private BigDecimal employmentFraction;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer paymentDayOfMonth;
    private boolean active;
    private Long categoryId;
    private String categoryName;

    private B2BConfigDTO b2bConfig;
    private UoPConfigDTO uopConfig;

    private String notes;

    // Wyliczone wartości
    private BigDecimal grossAmount; // kwota brutto z VAT (dla B2B)
    private BigDecimal netAmount;
    private BigDecimal totalDeductions;
}
