package pl.nehrebeccy.crmfinanse.model;

public enum VatRate {
    VAT_23(23, "23% - Podstawowa"),
    VAT_8(8, "8% - Obniżona"),
    VAT_5(5, "5% - Superobniżona"),
    VAT_0(0, "0% - Eksport"),
    EXEMPT(null, "Zwolnione z VAT");

    private final Integer rate;
    private final String displayName;

    VatRate(Integer rate, String displayName) {
        this.rate = rate;
        this.displayName = displayName;
    }

    public Integer getRate() {
        return rate;
    }

    public String getDisplayName() {
        return displayName;
    }
}
