package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.nehrebeccy.crmfinanse.model.CostRateType;

import java.math.BigDecimal;

/**
 * DTO dla konfiguracji Umowy Zlecenie.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UmowaZlecenieConfigDTO {

    private Long id;

    // === Składki ZUS ===
    private boolean withZus = true;
    private BigDecimal zusEmployee;
    private BigDecimal healthInsurance;

    // === Koszty uzyskania przychodu ===
    private CostRateType costRateType = CostRateType.STANDARD_20;
    private BigDecimal customCostRate;

    // === Podatek ===
    private BigDecimal incomeTax;
    private boolean pit2Filed;

    // === PPK ===
    private boolean ppk;
    private BigDecimal ppkRate;
}
