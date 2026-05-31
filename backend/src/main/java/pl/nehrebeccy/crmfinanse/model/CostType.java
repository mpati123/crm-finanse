package pl.nehrebeccy.crmfinanse.model;

import java.math.BigDecimal;

public enum CostType {
    STANDARD_250("Standardowe (250 zł)", new BigDecimal("250")),
    INCREASED_300("Podwyższone (300 zł)", new BigDecimal("300"));

    private final String displayName;
    private final BigDecimal amount;

    CostType(String displayName, BigDecimal amount) {
        this.displayName = displayName;
        this.amount = amount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
