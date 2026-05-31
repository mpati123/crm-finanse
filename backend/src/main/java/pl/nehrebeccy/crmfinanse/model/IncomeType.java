package pl.nehrebeccy.crmfinanse.model;

public enum IncomeType {
    UOP("Umowa o pracę"),
    UMOWA_ZLECENIE("Umowa zlecenie"),
    UMOWA_O_DZIELO("Umowa o dzieło"),
    B2B("Działalność gospodarcza"),
    SWIADCZENIE("Świadczenie"),
    CZYNSZ("Wynajem"),
    INNE("Inne");

    private final String displayName;

    IncomeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
