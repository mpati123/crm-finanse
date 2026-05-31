package pl.nehrebeccy.crmfinanse.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Typ kosztów uzyskania przychodu dla umów cywilnoprawnych.
 */
@Getter
@RequiredArgsConstructor
public enum CostRateType {
    STANDARD_20("Standardowe 20%", new BigDecimal("0.20")),
    AUTHOR_50("Autorskie 50%", new BigDecimal("0.50")),
    CUSTOM("Niestandardowe", null);

    private final String displayName;
    private final BigDecimal rate;
}
