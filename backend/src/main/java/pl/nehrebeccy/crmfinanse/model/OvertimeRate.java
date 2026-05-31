package pl.nehrebeccy.crmfinanse.model;

import java.math.BigDecimal;

/**
 * Stawki nadgodzin zgodne z polskim Kodeksem Pracy (art. 151^1)
 */
public enum OvertimeRate {
    // 100% - podstawowe nadgodziny (bez dodatku)
    RATE_100(new BigDecimal("1.00")),
    // 150% - za pracę w dni robocze i soboty
    RATE_150(new BigDecimal("1.50")),
    // 200% - za pracę w niedziele, święta i w nocy
    RATE_200(new BigDecimal("2.00"));

    private final BigDecimal multiplier;

    OvertimeRate(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }
}
