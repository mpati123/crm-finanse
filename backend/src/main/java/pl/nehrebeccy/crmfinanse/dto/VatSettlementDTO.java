package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VatSettlementDTO {

    private int year;
    private int month;

    // VAT naliczony (z wydatków/zakupów)
    private BigDecimal inputVatTotal;
    private List<VatRateBreakdown> inputVatByRate;

    // VAT należny (ze sprzedaży/przychodów)
    private BigDecimal outputVatTotal;
    private List<VatRateBreakdown> outputVatByRate;

    // Rozliczenie
    private BigDecimal vatToPay;      // do zapłaty (gdy należny > naliczony)
    private BigDecimal vatToReturn;   // do zwrotu (gdy naliczony > należny)

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VatRateBreakdown {
        private Integer rate;           // stawka VAT (23, 8, 5, 0, null dla zwolnionych)
        private BigDecimal netAmount;   // kwota netto
        private BigDecimal vatAmount;   // kwota VAT
        private BigDecimal grossAmount; // kwota brutto
    }
}
