package pl.nehrebeccy.crmfinanse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDTO {

    private Long id;

    @NotBlank(message = "Nazwa jest wymagana")
    private String name;

    @NotNull(message = "Kwota jest wymagana")
    @Positive(message = "Kwota musi być dodatnia")
    private BigDecimal amount;

    private Long categoryId;
    private String categoryName;

    @NotNull(message = "Data jest wymagana")
    private LocalDate date;

    @NotNull(message = "Status jest wymagany")
    private String status;

    private String notes;
    private boolean recurring;
}
