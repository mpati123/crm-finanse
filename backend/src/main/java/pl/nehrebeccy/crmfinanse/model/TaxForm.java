package pl.nehrebeccy.crmfinanse.model;

public enum TaxForm {
    SKALA("Skala podatkowa (12%/32%)"),
    LINIOWY("Podatek liniowy (19%)"),
    RYCZALT("Ryczałt");

    private final String displayName;

    TaxForm(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
