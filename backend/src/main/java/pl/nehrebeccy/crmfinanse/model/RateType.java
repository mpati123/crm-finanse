package pl.nehrebeccy.crmfinanse.model;

public enum RateType {
    MONTHLY("Miesiecznie"),
    HOURLY("Godzinowo");

    private final String displayName;

    RateType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
