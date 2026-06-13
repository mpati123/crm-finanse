package pl.nehrebeccy.crmfinanse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.nehrebeccy.crmfinanse.model.CostType;
import pl.nehrebeccy.crmfinanse.model.TaxForm;
import pl.nehrebeccy.crmfinanse.model.TaxPersonType;
import pl.nehrebeccy.crmfinanse.model.ZUSType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxPersonDTO {

    private Long id;
    private TaxPersonType type;

    // Dla INDIVIDUAL
    private String firstName;
    private String lastName;
    private String pesel;

    // Dla COMPANY
    private String companyName;
    private String nip;

    // Wspólne pole
    private String name;

    // === Konfiguracja PIT ===
    private TaxForm taxForm;
    private boolean pit2Filed;
    private CostType costType;
    private boolean jointTaxReturn;
    private Long spouseId;
    private String spouseName;

    // === Tracking roczny ===
    private int taxYear;
    private BigDecimal cumulativeGrossIncome;
    private BigDecimal cumulativeTaxableIncome;
    private BigDecimal cumulativeZusPaid;
    private boolean zusLimitReached;
    private boolean secondTaxBracket;

    // === Dla B2B - domyślne wartości ===
    private TaxForm defaultTaxForm;
    private ZUSType defaultZusType;
    private boolean defaultVatPayer;

    private boolean active;

    // === Obliczone pola (do wyświetlenia) ===
    private BigDecimal zusLimitProgress;        // Procent do limitu ZUS
    private BigDecimal taxThresholdProgress;    // Procent do progu podatkowego
    private BigDecimal remainingToZusLimit;     // Ile do limitu ZUS
    private BigDecimal remainingToTaxThreshold; // Ile do progu podatkowego

    // === Prognoza roczna ===
    private BigDecimal yearlyForecast;
}
