package pl.nehrebeccy.crmfinanse.model;

import java.math.BigDecimal;

public enum ZUSType {
    PELNY("Pełny ZUS", new BigDecimal("1600.32")),
    MALY_ZUS("Mały ZUS", new BigDecimal("402.65")),
    MALY_ZUS_PLUS("Mały ZUS Plus", new BigDecimal("889.00")),
    PREFERENCYJNY("Preferencyjny (pierwsze 2 lata)", new BigDecimal("402.65")),
    ULGA_NA_START("Ulga na start (6 miesięcy)", BigDecimal.ZERO),
    BEZ_ZUS("Bez ZUS", BigDecimal.ZERO);

    private final String displayName;
    private final BigDecimal defaultAmount;

    ZUSType(String displayName, BigDecimal defaultAmount) {
        this.displayName = displayName;
        this.defaultAmount = defaultAmount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getDefaultAmount() {
        return defaultAmount;
    }
}
