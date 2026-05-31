package pl.nehrebeccy.crmfinanse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Nazwa jest wymagana")
    private String name;

    private String color;
    private String icon;

    @NotNull(message = "Typ kategorii jest wymagany")
    private String type;
}
