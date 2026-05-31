package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.nehrebeccy.crmfinanse.model.TaxForm;
import pl.nehrebeccy.crmfinanse.model.ZUSType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2BConfigDTO {
    private Long id;
    private TaxForm taxForm;
    private ZUSType zusType;
    private boolean vatPayer;
    private Integer vatRate;
    private BigDecimal zusAmount;
    private BigDecimal healthInsurance;
    private BigDecimal incomeTaxAdvance;
    private BigDecimal ryczaltRate;
}
