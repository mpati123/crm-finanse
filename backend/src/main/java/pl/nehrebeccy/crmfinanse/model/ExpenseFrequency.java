package pl.nehrebeccy.crmfinanse.model;

public enum ExpenseFrequency {
    MONTHLY("Miesięcznie"),
    QUARTERLY("Kwartalnie"),
    YEARLY("Rocznie"),
    ONE_TIME("Jednorazowo");

    private final String displayName;

    ExpenseFrequency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
