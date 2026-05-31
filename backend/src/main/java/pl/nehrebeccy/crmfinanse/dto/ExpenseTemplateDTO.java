package pl.nehrebeccy.crmfinanse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.nehrebeccy.crmfinanse.model.ExpenseFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseTemplateDTO {

    private Long id;

    @NotBlank(message = "Nazwa jest wymagana")
    private String name;

    @NotNull(message = "Kwota jest wymagana")
    @Positive(message = "Kwota musi być dodatnia")
    private BigDecimal amount;

    private Long categoryId;
    private String categoryName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer dayOfMonth;

    @NotNull(message = "Częstotliwość jest wymagana")
    private ExpenseFrequency frequency;

    private boolean active;
    private String notes;
    private boolean autoPay;
}
