package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UoPConfigDTO {
    private Long id;
    private BigDecimal zusEmployee;
    private BigDecimal healthInsurance;
    private BigDecimal incomeTax;
    private boolean ppk;
    private BigDecimal ppkRate;
    private boolean authorCosts;
    private BigDecimal authorCostsPercentage;
}
