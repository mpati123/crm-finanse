package pl.nehrebeccy.crmfinanse.service;

import java.math.BigDecimal;

/**
 * Stałe podatkowe dla Polski na rok 2026.
 * Wartości należy aktualizować co roku zgodnie z obwieszczeniami.
 */
public final class PolishTaxConstants {

    private PolishTaxConstants() {
        // Klasa stałych - nie twórz instancji
    }

    // ============== OGÓLNE STAWKI PODATKOWE ==============

    /** Pierwsza stawka PIT (skala podatkowa) - 12% */
    public static final BigDecimal PIT_RATE_FIRST = new BigDecimal("0.12");

    /** Druga stawka PIT (skala podatkowa) - 32% */
    public static final BigDecimal PIT_RATE_SECOND = new BigDecimal("0.32");

    /** Stawka podatku liniowego (B2B) - 19% */
    public static final BigDecimal PIT_LINEAR = new BigDecimal("0.19");

    /** Próg podatkowy - po przekroczeniu stosuje się 32% */
    public static final BigDecimal TAX_THRESHOLD = new BigDecimal("120000");

    /** Kwota wolna od podatku rocznie */
    public static final BigDecimal TAX_FREE_AMOUNT = new BigDecimal("30000");

    /** Kwota zmniejszająca podatek miesięcznie (dla 12% PIT) = 30000 * 12% / 12 = 300 */
    public static final BigDecimal MONTHLY_TAX_REDUCTION = new BigDecimal("300");

    // ============== SKŁADKI ZUS - PRACOWNIK (UoP) ==============

    /** Składka emerytalna - pracownik */
    public static final BigDecimal ZUS_PENSION_EMPLOYEE = new BigDecimal("0.0976");

    /** Składka rentowa - pracownik */
    public static final BigDecimal ZUS_DISABILITY_EMPLOYEE = new BigDecimal("0.015");

    /** Składka chorobowa - pracownik */
    public static final BigDecimal ZUS_SICKNESS = new BigDecimal("0.0245");

    /** Suma składek społecznych pracownika (13.71%) */
    public static final BigDecimal ZUS_TOTAL_EMPLOYEE = new BigDecimal("0.1371");

    // ============== LIMIT ZUS (30-krotność) ==============

    /**
     * Przeciętne prognozowane wynagrodzenie 2026 (szacunkowe).
     * W 2025 było 8673 PLN, zakładam wzrost ~5%.
     * Należy zaktualizować po ogłoszeniu oficjalnej wartości.
     */
    public static final BigDecimal AVERAGE_SALARY_2026 = new BigDecimal("9107");

    /**
     * Roczny limit podstawy wymiaru składek na ubezpieczenia emerytalne i rentowe.
     * = 30 × przeciętne prognozowane wynagrodzenie
     * 30 × 9107 = 273 210 PLN
     */
    public static final BigDecimal ZUS_ANNUAL_LIMIT = AVERAGE_SALARY_2026.multiply(new BigDecimal("30"));

    // ============== SKŁADKA ZDROWOTNA ==============

    /** Składka zdrowotna dla UoP i skali podatkowej (B2B) - 9% */
    public static final BigDecimal HEALTH_INSURANCE_RATE = new BigDecimal("0.09");

    /** Składka zdrowotna dla podatku liniowego (B2B) - 4.9% */
    public static final BigDecimal HEALTH_INSURANCE_LINEAR = new BigDecimal("0.049");

    /** Minimalna składka zdrowotna miesięczna (2026) - szacunkowa */
    public static final BigDecimal MIN_HEALTH_INSURANCE = new BigDecimal("380.00");

    /** Roczny limit odliczenia składki zdrowotnej dla podatku liniowego (Polski Ład 2.0) */
    public static final BigDecimal HEALTH_INSURANCE_DEDUCTION_LINEAR_ANNUAL_LIMIT = new BigDecimal("8700");

    // ============== KOSZTY UZYSKANIA PRZYCHODU ==============

    /** Standardowe miesięczne koszty uzyskania przychodu (UoP, jeden pracodawca, w miejscu zamieszkania) */
    public static final BigDecimal STANDARD_COSTS = new BigDecimal("250");

    /** Podwyższone koszty uzyskania przychodu (poza miejscem zamieszkania) */
    public static final BigDecimal INCREASED_COSTS = new BigDecimal("300");

    /** Limit kosztów autorskich (50%) rocznie */
    public static final BigDecimal AUTHOR_COSTS_ANNUAL_LIMIT = new BigDecimal("120000");

    // ============== SKŁADKI ZUS B2B ==============

    /** Minimalna podstawa ZUS dla B2B - 60% przeciętnego wynagrodzenia */
    public static final BigDecimal B2B_ZUS_BASE_MIN = AVERAGE_SALARY_2026.multiply(new BigDecimal("0.60"));

    /**
     * Preferencyjny ZUS dla początkujących (pierwsze 24 miesiące).
     * 30% minimalnego wynagrodzenia.
     * Minimalne wynagrodzenie 2026 szacunkowe: 4666 PLN
     */
    public static final BigDecimal MIN_WAGE_2026 = new BigDecimal("4666");
    public static final BigDecimal B2B_ZUS_BASE_PREFERENTIAL = MIN_WAGE_2026.multiply(new BigDecimal("0.30"));

    // Pełne składki ZUS B2B (od 60% przeciętnego wynagrodzenia)
    // emerytalne 19.52% + rentowe 8% + chorobowe 2.45% + wypadkowe ~1.67% + FP 2.45% + FGŚP 0.10%

    /** Pełna składka społeczna B2B miesięcznie (~1800 PLN dla 2026) */
    public static final BigDecimal B2B_FULL_ZUS_MONTHLY = new BigDecimal("1900.00");

    /** Preferencyjna składka społeczna B2B miesięcznie (~350 PLN dla 2026) */
    public static final BigDecimal B2B_PREFERENTIAL_ZUS_MONTHLY = new BigDecimal("380.00");

    /** Mały ZUS Plus - zależny od przychodu roku poprzedniego, uproszczone ~900 PLN */
    public static final BigDecimal B2B_SMALL_ZUS_PLUS_MONTHLY = new BigDecimal("900.00");

    /** Bez ZUS (np. inny tytuł ubezpieczenia) */
    public static final BigDecimal B2B_NO_ZUS = BigDecimal.ZERO;

    // ============== RYCZAŁT ==============

    /** Domyślna stawka ryczałtu dla IT (12%) */
    public static final BigDecimal RYCZALT_IT_RATE = new BigDecimal("0.12");

    /** Limit przychodu dla ryczałtu rocznego */
    public static final BigDecimal RYCZALT_ANNUAL_LIMIT = new BigDecimal("2000000");

    // ============== POMOCNICZE METODY ==============

    /**
     * Oblicza kwotę zmniejszającą podatek w zależności od dochodu rocznego.
     * Dla skali podatkowej kwota wolna degresywnie zmniejsza się przy dochodach powyżej progu.
     */
    public static BigDecimal calculateTaxReduction(BigDecimal annualIncome) {
        if (annualIncome.compareTo(TAX_THRESHOLD) <= 0) {
            // Poniżej progu - pełna kwota zmniejszająca
            return TAX_FREE_AMOUNT.multiply(PIT_RATE_FIRST);
        } else {
            // Powyżej progu - kwota zmniejszająca wynosi 3600 zł (stała, nie degresywna w obecnym systemie)
            // W polskim systemie po przekroczeniu progu nadal odlicza się 3600 zł od podatku
            return TAX_FREE_AMOUNT.multiply(PIT_RATE_FIRST);
        }
    }

    /**
     * Sprawdza czy osiągnięto limit ZUS w danym miesiącu.
     * @param previousMonthsGross suma brutto z poprzednich miesięcy roku
     * @param currentMonthGross brutto za bieżący miesiąc
     * @return kwota podlegająca składkom ZUS (może być mniejsza niż brutto jeśli przekroczono limit)
     */
    public static BigDecimal calculateZusBase(BigDecimal previousMonthsGross, BigDecimal currentMonthGross) {
        BigDecimal totalWithCurrent = previousMonthsGross.add(currentMonthGross);

        if (previousMonthsGross.compareTo(ZUS_ANNUAL_LIMIT) >= 0) {
            // Już przekroczono limit - brak składek emerytalno-rentowych
            return BigDecimal.ZERO;
        }

        if (totalWithCurrent.compareTo(ZUS_ANNUAL_LIMIT) <= 0) {
            // Nie przekroczono limitu - pełna podstawa
            return currentMonthGross;
        }

        // Częściowo przekroczono limit w tym miesiącu
        return ZUS_ANNUAL_LIMIT.subtract(previousMonthsGross);
    }
}
