package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.nehrebeccy.crmfinanse.model.CostRateType;

import java.math.BigDecimal;

/**
 * DTO dla konfiguracji Umowy o Dzieło.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UmowaODzieloConfigDTO {

    private Long id;

    // === Koszty uzyskania przychodu ===
    private CostRateType costRateType = CostRateType.STANDARD_20;
    private BigDecimal customCostRate;

    // === Podatek ===
    private BigDecimal incomeTax;
}
