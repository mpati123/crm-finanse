package pl.nehrebeccy.crmfinanse.model;

public enum AmountType {
    GROSS("Brutto"),
    NET("Netto"),
    FIXED("Kwota stała");

    private final String displayName;

    AmountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
