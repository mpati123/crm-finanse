package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.nehrebeccy.crmfinanse.dto.IncomeSourceDTO;
import pl.nehrebeccy.crmfinanse.model.TaxForm;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Serwis do obliczania netto z uwzględnieniem polskiego prawa podatkowego.
 *
 * Uwzględnia:
 * - Limit 30-krotności ZUS (składki emerytalne i rentowe)
 * - Progresywną skalę podatkową (12% do 120k, 32% powyżej)
 * - Kwotę wolną od podatku (30k rocznie = 300 zł/mies. zmniejszenie podatku)
 * - Różne formy opodatkowania B2B (skala, liniowy, ryczałt)
 */
@Service
@RequiredArgsConstructor
public class IncomeCalculationService {

    /**
     * Wynik obliczenia podatku B2B z informacją o odliczonej składce zdrowotnej.
     */
    private static class B2BTaxResult {
        private final BigDecimal tax;
        private final BigDecimal healthInsuranceDeducted;

        public B2BTaxResult(BigDecimal tax, BigDecimal healthInsuranceDeducted) {
            this.tax = tax;
            this.healthInsuranceDeducted = healthInsuranceDeducted != null ? healthInsuranceDeducted : BigDecimal.ZERO;
        }

        public BigDecimal getTax() { return tax; }
        public BigDecimal getHealthInsuranceDeducted() { return healthInsuranceDeducted; }
    }

    /**
     * Kontekst roczny dla obliczeń - pozwala śledzić skumulowane wartości.
     */
    public static class YearlyContext {
        private int monthNumber;                    // 1-12
        private BigDecimal previousMonthsGrossSum;  // suma brutto z poprzednich miesięcy (dla limitu ZUS)
        private BigDecimal previousMonthsIncomeSum; // suma dochodów z poprzednich miesięcy (dla progu podatkowego)
        private BigDecimal previousMonthsHealthInsuranceDeducted; // suma odliczonej składki zdrowotnej (dla limitu 8700 PLN)

        public YearlyContext() {
            this.monthNumber = 1;
            this.previousMonthsGrossSum = BigDecimal.ZERO;
            this.previousMonthsIncomeSum = BigDecimal.ZERO;
            this.previousMonthsHealthInsuranceDeducted = BigDecimal.ZERO;
        }

        public YearlyContext(int monthNumber, BigDecimal previousMonthsGrossSum, BigDecimal previousMonthsIncomeSum) {
            this.monthNumber = monthNumber;
            this.previousMonthsGrossSum = previousMonthsGrossSum != null ? previousMonthsGrossSum : BigDecimal.ZERO;
            this.previousMonthsIncomeSum = previousMonthsIncomeSum != null ? previousMonthsIncomeSum : BigDecimal.ZERO;
            this.previousMonthsHealthInsuranceDeducted = BigDecimal.ZERO;
        }

        public YearlyContext(int monthNumber, BigDecimal previousMonthsGrossSum, BigDecimal previousMonthsIncomeSum,
                             BigDecimal previousMonthsHealthInsuranceDeducted) {
            this.monthNumber = monthNumber;
            this.previousMonthsGrossSum = previousMonthsGrossSum != null ? previousMonthsGrossSum : BigDecimal.ZERO;
            this.previousMonthsIncomeSum = previousMonthsIncomeSum != null ? previousMonthsIncomeSum : BigDecimal.ZERO;
            this.previousMonthsHealthInsuranceDeducted = previousMonthsHealthInsuranceDeducted != null ?
                previousMonthsHealthInsuranceDeducted : BigDecimal.ZERO;
        }

        public int getMonthNumber() { return monthNumber; }
        public BigDecimal getPreviousMonthsGrossSum() { return previousMonthsGrossSum; }
        public BigDecimal getPreviousMonthsIncomeSum() { return previousMonthsIncomeSum; }
        public BigDecimal getPreviousMonthsHealthInsuranceDeducted() { return previousMonthsHealthInsuranceDeducted; }
    }

    /**
     * Oblicza netto dla źródła przychodu bez kontekstu rocznego (dla kompatybilności wstecznej).
     * Zakłada styczeń (miesiąc 1) i brak poprzednich dochodów.
     */
    public IncomeSourceDTO calculateNetIncome(IncomeSourceDTO dto) {
        return calculateNetIncome(dto, new YearlyContext());
    }

    /**
     * Oblicza netto dla źródła przychodu z uwzględnieniem kontekstu rocznego.
     *
     * @param dto źródło przychodu
     * @param context kontekst roczny (numer miesiąca, skumulowane dochody)
     * @return DTO z obliczonymi wartościami netto
     */
    public IncomeSourceDTO calculateNetIncome(IncomeSourceDTO dto, YearlyContext context) {
        BigDecimal netAmount;
        BigDecimal grossAmount = dto.getAmount();

        switch (dto.getIncomeType()) {
            case UOP:
                netAmount = calculateUoPNet(dto, context);
                break;
            case B2B:
                netAmount = calculateB2BNet(dto, context);
                if (dto.getB2bConfig() != null && dto.getB2bConfig().isVatPayer()) {
                    Integer vatRate = dto.getB2bConfig().getVatRate();
                    if (vatRate == null) {
                        vatRate = 23;
                    }
                    BigDecimal vatMultiplier = BigDecimal.ONE.add(
                        new BigDecimal(vatRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    );
                    grossAmount = dto.getAmount().multiply(vatMultiplier).setScale(2, RoundingMode.HALF_UP);
                }
                break;
            case UMOWA_ZLECENIE:
                netAmount = calculateUmowaZlecenieNet(dto, context);
                break;
            case UMOWA_O_DZIELO:
                netAmount = calculateUmowaODzieloNet(dto, context);
                break;
            case SWIADCZENIE:
            case CZYNSZ:
            case INNE:
            default:
                netAmount = dto.getAmount();
                break;
        }

        BigDecimal totalDeductions = dto.getAmount().subtract(netAmount);
        dto.setGrossAmount(grossAmount);
        dto.setNetAmount(netAmount);
        dto.setTotalDeductions(totalDeductions);
        return dto;
    }

    /**
     * Oblicza netto dla umowy o pracę z uwzględnieniem:
     * - Limitu 30-krotności ZUS
     * - Progresywnej skali podatkowej
     * - Kwoty wolnej od podatku
     */
    private BigDecimal calculateUoPNet(IncomeSourceDTO dto, YearlyContext context) {
        BigDecimal brutto = dto.getAmount();

        // Jeśli mamy ręcznie wpisane wartości, użyj ich
        if (dto.getUopConfig() != null &&
            dto.getUopConfig().getZusEmployee() != null &&
            dto.getUopConfig().getHealthInsurance() != null &&
            dto.getUopConfig().getIncomeTax() != null) {

            BigDecimal netto = brutto
                    .subtract(dto.getUopConfig().getZusEmployee())
                    .subtract(dto.getUopConfig().getHealthInsurance())
                    .subtract(dto.getUopConfig().getIncomeTax());

            if (dto.getUopConfig().isPpk() && dto.getUopConfig().getPpkRate() != null) {
                BigDecimal ppk = brutto.multiply(dto.getUopConfig().getPpkRate().divide(BigDecimal.valueOf(100)));
                netto = netto.subtract(ppk);
            }

            return netto.setScale(2, RoundingMode.HALF_UP);
        }

        // ============ OBLICZENIA AUTOMATYCZNE Z KONTEKSTEM ROCZNYM ============

        // 1. Oblicz podstawę ZUS z uwzględnieniem limitu 30-krotności
        BigDecimal zusBase = PolishTaxConstants.calculateZusBase(
            context.getPreviousMonthsGrossSum(),
            brutto
        );

        // 2. Składki ZUS pracownika (emerytalna + rentowa tylko do limitu)
        BigDecimal zusEmerytalneRentowe = zusBase.multiply(
            PolishTaxConstants.ZUS_PENSION_EMPLOYEE.add(PolishTaxConstants.ZUS_DISABILITY_EMPLOYEE)
        );
        // Składka chorobowa - zawsze od pełnej kwoty brutto (bez limitu)
        BigDecimal zusChorobowe = brutto.multiply(PolishTaxConstants.ZUS_SICKNESS);
        BigDecimal zusEmployee = zusEmerytalneRentowe.add(zusChorobowe);

        // 3. Podstawa składki zdrowotnej = brutto - ZUS społeczny
        BigDecimal podstawaZdrowotna = brutto.subtract(zusEmployee);
        BigDecimal skladkaZdrowotna = podstawaZdrowotna.multiply(PolishTaxConstants.HEALTH_INSURANCE_RATE);

        // 4. Koszty uzyskania przychodu
        BigDecimal koszty = PolishTaxConstants.STANDARD_COSTS;
        if (dto.getUopConfig() != null && dto.getUopConfig().isAuthorCosts()) {
            BigDecimal authorCostsPercent = dto.getUopConfig().getAuthorCostsPercentage();
            if (authorCostsPercent == null) {
                authorCostsPercent = new BigDecimal("50");
            }
            // 50% kosztów autorskich, ale sprawdź limit roczny
            BigDecimal authorCosts = podstawaZdrowotna.multiply(authorCostsPercent.divide(BigDecimal.valueOf(100)));

            // Sprawdź czy nie przekroczyliśmy limitu kosztów autorskich (120k rocznie)
            BigDecimal previousAuthorCosts = context.getPreviousMonthsGrossSum()
                .multiply(authorCostsPercent.divide(BigDecimal.valueOf(100)));
            BigDecimal remainingLimit = PolishTaxConstants.AUTHOR_COSTS_ANNUAL_LIMIT.subtract(previousAuthorCosts);

            if (remainingLimit.compareTo(BigDecimal.ZERO) <= 0) {
                // Limit wyczerpany - standardowe koszty
                koszty = PolishTaxConstants.STANDARD_COSTS;
            } else if (authorCosts.compareTo(remainingLimit) > 0) {
                // Częściowo w limicie
                koszty = remainingLimit;
            } else {
                koszty = authorCosts;
            }
        }

        // 5. Dochód miesięczny
        BigDecimal dochodMiesieczny = podstawaZdrowotna.subtract(koszty);
        if (dochodMiesieczny.compareTo(BigDecimal.ZERO) < 0) {
            dochodMiesieczny = BigDecimal.ZERO;
        }

        // 6. Oblicz zaliczkę na PIT z progresywną skalą
        BigDecimal podatek = calculateProgressiveTax(dochodMiesieczny, context.getPreviousMonthsIncomeSum());

        // 7. Netto
        BigDecimal netto = brutto
                .subtract(zusEmployee)
                .subtract(skladkaZdrowotna)
                .subtract(podatek);

        // 8. PPK jeśli uczestniczy
        if (dto.getUopConfig() != null && dto.getUopConfig().isPpk()) {
            BigDecimal ppkRate = dto.getUopConfig().getPpkRate();
            if (ppkRate == null) {
                ppkRate = new BigDecimal("2");
            }
            BigDecimal ppk = brutto.multiply(ppkRate.divide(BigDecimal.valueOf(100)));
            netto = netto.subtract(ppk);
        }

        return netto.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Oblicza zaliczkę na PIT z uwzględnieniem progresywnej skali.
     *
     * Polskie prawo podatkowe (Polski Ład):
     * - Do 120 000 PLN dochodu rocznie: 12%
     * - Powyżej 120 000 PLN: 32%
     * - Kwota zmniejszająca podatek: 300 PLN miesięcznie (3 600 PLN rocznie)
     *   stosowana PRZEZ CAŁY ROK, niezależnie od progu!
     *
     * @param currentMonthIncome dochód za bieżący miesiąc
     * @param previousMonthsIncome suma dochodów z poprzednich miesięcy
     * @return zaliczka na PIT za bieżący miesiąc
     */
    private BigDecimal calculateProgressiveTax(BigDecimal currentMonthIncome, BigDecimal previousMonthsIncome) {
        BigDecimal totalIncomeWithCurrent = previousMonthsIncome.add(currentMonthIncome);
        BigDecimal threshold = PolishTaxConstants.TAX_THRESHOLD;
        BigDecimal monthlyTaxReduction = PolishTaxConstants.MONTHLY_TAX_REDUCTION;

        BigDecimal podatek;

        if (previousMonthsIncome.compareTo(threshold) >= 0) {
            // Już w drugim progu od początku miesiąca - cały dochód 32%
            // Kwota zmniejszająca NADAL się stosuje!
            podatek = currentMonthIncome.multiply(PolishTaxConstants.PIT_RATE_SECOND)
                    .subtract(monthlyTaxReduction);
        } else if (totalIncomeWithCurrent.compareTo(threshold) <= 0) {
            // Cały rok w pierwszym progu - 12% minus kwota zmniejszająca
            podatek = currentMonthIncome.multiply(PolishTaxConstants.PIT_RATE_FIRST)
                    .subtract(monthlyTaxReduction);
        } else {
            // Przekroczenie progu w tym miesiącu - część 12%, część 32%
            BigDecimal incomeInFirstBracket = threshold.subtract(previousMonthsIncome);
            BigDecimal incomeInSecondBracket = currentMonthIncome.subtract(incomeInFirstBracket);

            BigDecimal taxFirstBracket = incomeInFirstBracket.multiply(PolishTaxConstants.PIT_RATE_FIRST);
            BigDecimal taxSecondBracket = incomeInSecondBracket.multiply(PolishTaxConstants.PIT_RATE_SECOND);

            // Kwota zmniejszająca stosowana przez cały rok
            podatek = taxFirstBracket.add(taxSecondBracket).subtract(monthlyTaxReduction);
        }

        if (podatek.compareTo(BigDecimal.ZERO) < 0) {
            podatek = BigDecimal.ZERO;
        }

        return podatek.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Oblicza netto dla B2B z uwzględnieniem formy opodatkowania i kontekstu rocznego.
     */
    private BigDecimal calculateB2BNet(IncomeSourceDTO dto, YearlyContext context) {
        if (dto.getB2bConfig() == null) {
            return dto.getAmount();
        }

        BigDecimal przychod = dto.getAmount();

        // 1. ZUS społeczny (stała kwota zależna od typu)
        BigDecimal zus = dto.getB2bConfig().getZusAmount();
        if (zus == null) {
            zus = dto.getB2bConfig().getZusType().getDefaultAmount();
        }

        // 2. Dochód = Przychód - ZUS
        BigDecimal dochod = przychod.subtract(zus);
        if (dochod.compareTo(BigDecimal.ZERO) < 0) {
            dochod = BigDecimal.ZERO;
        }

        // 3. Składka zdrowotna - zależy od formy opodatkowania
        BigDecimal zdrowotna = dto.getB2bConfig().getHealthInsurance();
        if (zdrowotna == null) {
            zdrowotna = calculateB2BHealthInsurance(dto.getB2bConfig().getTaxForm(), dochod);
        }

        // 4. Podatek dochodowy (Polski Ład 2.0: uwzględnia odliczenie składki zdrowotnej)
        BigDecimal podatek = dto.getB2bConfig().getIncomeTaxAdvance();
        if (podatek == null) {
            B2BTaxResult taxResult = calculateB2BTax(dto, dochod, przychod, zdrowotna, context);
            podatek = taxResult.getTax();
            // healthInsuranceDeducted z wyniku jest używane tylko przy symulacjach rocznych
        }

        // 5. Netto = Przychód - ZUS - Zdrowotna - Podatek
        BigDecimal netto = przychod
                .subtract(zus)
                .subtract(zdrowotna)
                .subtract(podatek);

        return netto.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Oblicza składkę zdrowotną dla B2B w zależności od formy opodatkowania.
     */
    private BigDecimal calculateB2BHealthInsurance(TaxForm taxForm, BigDecimal dochod) {
        BigDecimal zdrowotna;
        BigDecimal minZdrowotna = PolishTaxConstants.MIN_HEALTH_INSURANCE;

        if (taxForm == TaxForm.LINIOWY) {
            // Dla liniowego 4.9% od dochodu
            zdrowotna = dochod.multiply(PolishTaxConstants.HEALTH_INSURANCE_LINEAR);
            if (zdrowotna.compareTo(minZdrowotna) < 0) {
                zdrowotna = minZdrowotna;
            }
        } else if (taxForm == TaxForm.RYCZALT) {
            // Ryczałt - składka zryczałtowana (uproszczenie - zależy od przychodu rocznego)
            zdrowotna = new BigDecimal("420");
        } else {
            // Skala podatkowa - 9% od dochodu
            zdrowotna = dochod.multiply(PolishTaxConstants.HEALTH_INSURANCE_RATE);
            if (zdrowotna.compareTo(minZdrowotna) < 0) {
                zdrowotna = minZdrowotna;
            }
        }

        return zdrowotna.max(BigDecimal.ZERO);
    }

    /**
     * Oblicza podatek dla B2B z uwzględnieniem formy opodatkowania i kontekstu rocznego.
     * Implementuje Polski Ład 2.0:
     * - LINIOWY (19%): składka zdrowotna odliczana od dochodu do 8 700 PLN rocznie
     * - RYCZAŁT: 50% składki zdrowotnej odliczane od przychodu
     * - SKALA: składka zdrowotna NIE odliczana
     *
     * @param dto źródło przychodu
     * @param dochod dochód (przychód - ZUS)
     * @param przychod przychód brutto
     * @param healthInsurance składka zdrowotna za bieżący miesiąc
     * @param context kontekst roczny
     * @return wynik z podatkiem i kwotą odliczonej składki zdrowotnej
     */
    private B2BTaxResult calculateB2BTax(IncomeSourceDTO dto, BigDecimal dochod, BigDecimal przychod,
                                         BigDecimal healthInsurance, YearlyContext context) {
        TaxForm taxForm = dto.getB2bConfig().getTaxForm();
        BigDecimal podatek;
        BigDecimal healthInsuranceDeducted = BigDecimal.ZERO;

        if (taxForm == TaxForm.LINIOWY) {
            // POLSKI ŁAD 2.0: Dla podatku liniowego składka zdrowotna odliczana od dochodu do 8 700 PLN rocznie
            BigDecimal remainingLimit = PolishTaxConstants.HEALTH_INSURANCE_DEDUCTION_LINEAR_ANNUAL_LIMIT
                .subtract(context.getPreviousMonthsHealthInsuranceDeducted());

            if (remainingLimit.compareTo(BigDecimal.ZERO) > 0) {
                healthInsuranceDeducted = healthInsurance.min(remainingLimit);
            }

            // Podstawa opodatkowania = dochód - odliczona składka zdrowotna
            BigDecimal taxBase = dochod.subtract(healthInsuranceDeducted);
            if (taxBase.compareTo(BigDecimal.ZERO) < 0) {
                taxBase = BigDecimal.ZERO;
            }

            // 19% od podstawy
            podatek = taxBase.multiply(PolishTaxConstants.PIT_LINEAR);
        } else if (taxForm == TaxForm.RYCZALT) {
            // POLSKI ŁAD 2.0: Dla ryczałtu 50% składki zdrowotnej odliczane od przychodu
            healthInsuranceDeducted = healthInsurance.multiply(new BigDecimal("0.5"));

            // Podstawa opodatkowania = przychód - 50% składki zdrowotnej
            BigDecimal taxBase = przychod.subtract(healthInsuranceDeducted);
            if (taxBase.compareTo(BigDecimal.ZERO) < 0) {
                taxBase = BigDecimal.ZERO;
            }

            // Procent od podstawy (np. 12% dla IT)
            BigDecimal ryczaltRate = dto.getB2bConfig().getRyczaltRate();
            if (ryczaltRate == null) {
                ryczaltRate = PolishTaxConstants.RYCZALT_IT_RATE.multiply(BigDecimal.valueOf(100));
            }
            podatek = taxBase.multiply(ryczaltRate.divide(BigDecimal.valueOf(100)));

            // Dla ryczałtu nie ma limitu rocznego odliczenia (zawsze 50%)
        } else {
            // SKALA PODATKOWA: składka zdrowotna NIE jest odliczana (Polski Ład 2.0)
            // healthInsuranceDeducted pozostaje 0
            podatek = calculateProgressiveTax(dochod, context.getPreviousMonthsIncomeSum());
        }

        return new B2BTaxResult(
            podatek.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
            healthInsuranceDeducted.setScale(2, RoundingMode.HALF_UP)
        );
    }

    /**
     * Oblicza netto dla umowy zlecenie z uwzględnieniem limitu ZUS.
     */
    private BigDecimal calculateUmowaZlecenieNet(IncomeSourceDTO dto, YearlyContext context) {
        BigDecimal brutto = dto.getAmount();

        // Jeśli mamy ręcznie wpisane wartości, użyj ich
        if (dto.getUmowaZlecenieConfig() != null &&
            dto.getUmowaZlecenieConfig().getZusEmployee() != null &&
            dto.getUmowaZlecenieConfig().getHealthInsurance() != null &&
            dto.getUmowaZlecenieConfig().getIncomeTax() != null) {

            BigDecimal netto = brutto
                    .subtract(dto.getUmowaZlecenieConfig().getZusEmployee())
                    .subtract(dto.getUmowaZlecenieConfig().getHealthInsurance())
                    .subtract(dto.getUmowaZlecenieConfig().getIncomeTax());

            if (dto.getUmowaZlecenieConfig().isPpk() && dto.getUmowaZlecenieConfig().getPpkRate() != null) {
                BigDecimal ppk = brutto.multiply(dto.getUmowaZlecenieConfig().getPpkRate().divide(BigDecimal.valueOf(100)));
                netto = netto.subtract(ppk);
            }

            return netto.setScale(2, RoundingMode.HALF_UP);
        }

        // ============ OBLICZENIA AUTOMATYCZNE ============

        // Sprawdź czy umowa ma ZUS (opcjonalne dla umowy zlecenie)
        boolean withZus = dto.getUmowaZlecenieConfig() == null || dto.getUmowaZlecenieConfig().isWithZus();

        BigDecimal zusEmployee = BigDecimal.ZERO;
        BigDecimal podstawaZdrowotna = brutto;

        if (withZus) {
            // Podstawa ZUS z limitem 30-krotności
            BigDecimal zusBase = PolishTaxConstants.calculateZusBase(
                context.getPreviousMonthsGrossSum(),
                brutto
            );

            // Składki ZUS
            BigDecimal zusEmerytalneRentowe = zusBase.multiply(
                PolishTaxConstants.ZUS_PENSION_EMPLOYEE.add(PolishTaxConstants.ZUS_DISABILITY_EMPLOYEE)
            );
            BigDecimal zusChorobowe = brutto.multiply(PolishTaxConstants.ZUS_SICKNESS);
            zusEmployee = zusEmerytalneRentowe.add(zusChorobowe);

            // Podstawa składki zdrowotnej
            podstawaZdrowotna = brutto.subtract(zusEmployee);
        }

        BigDecimal skladkaZdrowotna = podstawaZdrowotna.multiply(PolishTaxConstants.HEALTH_INSURANCE_RATE);

        // Koszty uzyskania - z konfiguracji lub domyślne 20%
        BigDecimal kosztyRate = new BigDecimal("0.20");
        if (dto.getUmowaZlecenieConfig() != null && dto.getUmowaZlecenieConfig().getCostRateType() != null) {
            if (dto.getUmowaZlecenieConfig().getCostRateType().getRate() != null) {
                kosztyRate = dto.getUmowaZlecenieConfig().getCostRateType().getRate();
            } else if (dto.getUmowaZlecenieConfig().getCustomCostRate() != null) {
                kosztyRate = dto.getUmowaZlecenieConfig().getCustomCostRate();
            }
        }
        BigDecimal koszty = podstawaZdrowotna.multiply(kosztyRate);

        // Dochód
        BigDecimal dochod = podstawaZdrowotna.subtract(koszty);
        if (dochod.compareTo(BigDecimal.ZERO) < 0) {
            dochod = BigDecimal.ZERO;
        }

        // Zaliczka na PIT - progresywna (z uwzględnieniem PIT-2 jeśli złożony)
        BigDecimal podatek = calculateProgressiveTax(dochod, context.getPreviousMonthsIncomeSum());
        if (dto.getUmowaZlecenieConfig() != null && !dto.getUmowaZlecenieConfig().isPit2Filed()) {
            // Bez PIT-2 - nie stosujemy kwoty zmniejszającej (dodajemy ją z powrotem)
            podatek = podatek.add(PolishTaxConstants.MONTHLY_TAX_REDUCTION);
        }

        // Netto
        BigDecimal netto = brutto
                .subtract(zusEmployee)
                .subtract(skladkaZdrowotna)
                .subtract(podatek);

        // PPK jeśli uczestniczy
        if (dto.getUmowaZlecenieConfig() != null && dto.getUmowaZlecenieConfig().isPpk()) {
            BigDecimal ppkRate = dto.getUmowaZlecenieConfig().getPpkRate();
            if (ppkRate == null) {
                ppkRate = new BigDecimal("2");
            }
            BigDecimal ppk = brutto.multiply(ppkRate.divide(BigDecimal.valueOf(100)));
            netto = netto.subtract(ppk);
        }

        return netto.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Oblicza netto dla umowy o dzieło.
     * Umowa o dzieło nie ma składek ZUS, ale ma progresywny podatek.
     */
    private BigDecimal calculateUmowaODzieloNet(IncomeSourceDTO dto, YearlyContext context) {
        BigDecimal brutto = dto.getAmount();

        // Jeśli mamy ręcznie wpisany podatek, użyj go
        if (dto.getUmowaODzieloConfig() != null && dto.getUmowaODzieloConfig().getIncomeTax() != null) {
            BigDecimal netto = brutto.subtract(dto.getUmowaODzieloConfig().getIncomeTax());
            return netto.setScale(2, RoundingMode.HALF_UP);
        }

        // ============ OBLICZENIA AUTOMATYCZNE ============

        // Koszty uzyskania - z konfiguracji lub domyślne 20%
        BigDecimal kosztyRate = new BigDecimal("0.20");
        if (dto.getUmowaODzieloConfig() != null && dto.getUmowaODzieloConfig().getCostRateType() != null) {
            if (dto.getUmowaODzieloConfig().getCostRateType().getRate() != null) {
                kosztyRate = dto.getUmowaODzieloConfig().getCostRateType().getRate();
            } else if (dto.getUmowaODzieloConfig().getCustomCostRate() != null) {
                kosztyRate = dto.getUmowaODzieloConfig().getCustomCostRate();
            }
        }
        BigDecimal koszty = brutto.multiply(kosztyRate);

        // Dochód
        BigDecimal dochod = brutto.subtract(koszty);

        // Zaliczka na PIT - progresywna
        BigDecimal podatek = calculateProgressiveTax(dochod, context.getPreviousMonthsIncomeSum());

        // Netto = Brutto - Podatek (brak ZUS w umowie o dzieło)
        BigDecimal netto = brutto.subtract(podatek);

        return netto.setScale(2, RoundingMode.HALF_UP);
    }

    // ============ METODY POMOCNICZE DLA SYMULACJI ROCZNEJ ============
    /**
     * Symuluje obliczenia dla całego roku z uwzględnieniem rzeczywistych przychodów.
     * Używa rzeczywistych kwot (actualAmount) do kumulowania wartości przy obliczaniu progów podatkowych.
     *
     * @param dto źródło przychodu
     * @param year rok do symulacji
     * @param actualIncomes lista rzeczywistych przychodów miesięcznych
     * @return tablica 12 wartości netto dla każdego miesiąca
     */
    public BigDecimal[] simulateYearlyNetIncome(IncomeSourceDTO dto, int year, List<pl.nehrebeccy.crmfinanse.dto.IncomeDTO> actualIncomes) {
        // Utwórz mapę month -> actualAmount
        Map<Integer, BigDecimal> actualAmounts = new HashMap<>();
        if (actualIncomes != null) {
            for (pl.nehrebeccy.crmfinanse.dto.IncomeDTO income : actualIncomes) {
                if (income.getActualAmount() != null && income.getDate() != null) {
                    actualAmounts.put(income.getDate().getMonthValue(), income.getActualAmount());
                }
            }
        }

        BigDecimal[] monthlyNet = new BigDecimal[12];
        BigDecimal cumulativeGross = BigDecimal.ZERO;
        BigDecimal cumulativeIncome = BigDecimal.ZERO;
        BigDecimal cumulativeHealthInsuranceDeducted = BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            // Sprawdź czy miesiąc mieści się w zakresie dat źródła przychodu
            LocalDate monthDate = LocalDate.of(year, month, 1);

            boolean isActive = true;
            if (dto.getStartDate() != null && monthDate.isBefore(dto.getStartDate().withDayOfMonth(1))) {
                isActive = false;
            }
            if (dto.getEndDate() != null && monthDate.isAfter(dto.getEndDate().withDayOfMonth(1))) {
                isActive = false;
            }

            if (!isActive) {
                monthlyNet[month - 1] = BigDecimal.ZERO;
                continue;
            }

            // Użyj rzeczywistej kwoty jeśli dostępna, w przeciwnym razie szacunkową
            BigDecimal monthlyGross = actualAmounts.getOrDefault(month, dto.getAmount());

            YearlyContext context = new YearlyContext(month, cumulativeGross, cumulativeIncome, cumulativeHealthInsuranceDeducted);

            // Tworzymy kopię DTO dla obliczeń
            IncomeSourceDTO monthDto = copyDto(dto);
            // Tymczasowo ustaw amount na rzeczywistą wartość dla tego miesiąca
            monthDto.setAmount(monthlyGross);

            // Dla B2B - musimy śledzić odliczoną składkę zdrowotną
            if (dto.getIncomeType() == pl.nehrebeccy.crmfinanse.model.IncomeType.B2B && dto.getB2bConfig() != null) {
                // Oblicz składkę zdrowotną dla tego miesiąca
                BigDecimal zus = dto.getB2bConfig().getZusAmount();
                if (zus == null) {
                    zus = dto.getB2bConfig().getZusType().getDefaultAmount();
                }
                BigDecimal dochod = monthlyGross.subtract(zus).max(BigDecimal.ZERO);
                BigDecimal zdrowotna = dto.getB2bConfig().getHealthInsurance();
                if (zdrowotna == null) {
                    zdrowotna = calculateB2BHealthInsurance(dto.getB2bConfig().getTaxForm(), dochod);
                }

                // Oblicz podatek i śledź odliczenie
                B2BTaxResult taxResult = calculateB2BTax(monthDto, dochod, monthlyGross, zdrowotna, context);
                cumulativeHealthInsuranceDeducted = cumulativeHealthInsuranceDeducted.add(taxResult.getHealthInsuranceDeducted());
            }

            calculateNetIncome(monthDto, context);
            monthlyNet[month - 1] = monthDto.getNetAmount();

            // Aktualizuj skumulowane wartości używając rzeczywistych kwot
            cumulativeGross = cumulativeGross.add(monthlyGross);

            // Dla dochodu - oblicz na podstawie rzeczywistej kwoty
            BigDecimal originalAmount = dto.getAmount();
            dto.setAmount(monthlyGross);
            BigDecimal monthlyIncome = calculateMonthlyIncome(dto);
            dto.setAmount(originalAmount);
            cumulativeIncome = cumulativeIncome.add(monthlyIncome);
        }

        return monthlyNet;
    }



    /**
     * Symuluje obliczenia dla całego roku i zwraca tablicę netto dla każdego miesiąca.
     * Przydatne do pokazania jak zmienia się netto w ciągu roku.
     *
     * @param dto źródło przychodu (zakładając stałą kwotę przez cały rok)
     * @return tablica 12 wartości netto dla każdego miesiąca
     */
    /**
     * Symuluje obliczenia dla całego roku i zwraca tablicę netto dla każdego miesiąca.
     * Uwzględnia startDate i endDate źródła przychodu.
     *
     * @param dto źródło przychodu
     * @param year rok do symulacji (np. 2026)
     * @return tablica 12 wartości netto dla każdego miesiąca (0 dla miesięcy poza zakresem)
     */
    public BigDecimal[] simulateYearlyNetIncome(IncomeSourceDTO dto, int year) {
        BigDecimal[] monthlyNet = new BigDecimal[12];
        BigDecimal cumulativeGross = BigDecimal.ZERO;
        BigDecimal cumulativeIncome = BigDecimal.ZERO;
        BigDecimal cumulativeHealthInsuranceDeducted = BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            // Sprawdź czy miesiąc mieści się w zakresie dat źródła przychodu
            LocalDate monthDate = LocalDate.of(year, month, 1);

            boolean isActive = true;
            if (dto.getStartDate() != null && monthDate.isBefore(dto.getStartDate().withDayOfMonth(1))) {
                isActive = false;
            }
            if (dto.getEndDate() != null && monthDate.isAfter(dto.getEndDate().withDayOfMonth(1))) {
                isActive = false;
            }

            if (!isActive) {
                monthlyNet[month - 1] = BigDecimal.ZERO;
                continue;
            }

            YearlyContext context = new YearlyContext(month, cumulativeGross, cumulativeIncome, cumulativeHealthInsuranceDeducted);

            // Tworzymy kopię DTO dla obliczeń
            IncomeSourceDTO monthDto = copyDto(dto);

            // Dla B2B - musimy śledzić odliczoną składkę zdrowotną
            if (dto.getIncomeType() == pl.nehrebeccy.crmfinanse.model.IncomeType.B2B && dto.getB2bConfig() != null) {
                // Oblicz składkę zdrowotną dla tego miesiąca
                BigDecimal zus = dto.getB2bConfig().getZusAmount();
                if (zus == null) {
                    zus = dto.getB2bConfig().getZusType().getDefaultAmount();
                }
                BigDecimal dochod = dto.getAmount().subtract(zus).max(BigDecimal.ZERO);
                BigDecimal zdrowotna = dto.getB2bConfig().getHealthInsurance();
                if (zdrowotna == null) {
                    zdrowotna = calculateB2BHealthInsurance(dto.getB2bConfig().getTaxForm(), dochod);
                }

                // Oblicz podatek i śledź odliczenie
                B2BTaxResult taxResult = calculateB2BTax(monthDto, dochod, dto.getAmount(), zdrowotna, context);
                cumulativeHealthInsuranceDeducted = cumulativeHealthInsuranceDeducted.add(taxResult.getHealthInsuranceDeducted());
            }

            calculateNetIncome(monthDto, context);
            monthlyNet[month - 1] = monthDto.getNetAmount();

            // Aktualizuj skumulowane wartości tylko dla aktywnych miesięcy
            cumulativeGross = cumulativeGross.add(dto.getAmount());

            // Dla dochodu - zależy od typu
            BigDecimal monthlyIncome = calculateMonthlyIncome(dto);
            cumulativeIncome = cumulativeIncome.add(monthlyIncome);
        }

        return monthlyNet;
    }

    /**
     * Przeciążona metoda dla kompatybilności - używa roku z startDate lub bieżącego
     */
    public BigDecimal[] simulateYearlyNetIncome(IncomeSourceDTO dto) {
        int year = dto.getStartDate() != null 
            ? dto.getStartDate().getYear() 
            : LocalDate.now().getYear();
        return simulateYearlyNetIncome(dto, year);
    }


    /**
     * Oblicza miesięczny dochód (do śledzenia progu podatkowego).
     */
    private BigDecimal calculateMonthlyIncome(IncomeSourceDTO dto) {
        BigDecimal brutto = dto.getAmount();

        switch (dto.getIncomeType()) {
            case UOP:
                // Dochód = brutto - ZUS - koszty (uproszczenie)
                BigDecimal zusUop = brutto.multiply(PolishTaxConstants.ZUS_TOTAL_EMPLOYEE);
                BigDecimal podstawaUop = brutto.subtract(zusUop);
                BigDecimal kosztyUop = PolishTaxConstants.STANDARD_COSTS;
                return podstawaUop.subtract(kosztyUop).max(BigDecimal.ZERO);
            case UMOWA_ZLECENIE:
                // Sprawdź czy ma ZUS
                boolean withZus = dto.getUmowaZlecenieConfig() == null || dto.getUmowaZlecenieConfig().isWithZus();
                BigDecimal podstawaZlecenie = brutto;
                if (withZus) {
                    BigDecimal zusZlecenie = brutto.multiply(PolishTaxConstants.ZUS_TOTAL_EMPLOYEE);
                    podstawaZlecenie = brutto.subtract(zusZlecenie);
                }
                // Koszty z konfiguracji lub domyślne 20%
                BigDecimal kosztyRateZlecenie = new BigDecimal("0.20");
                if (dto.getUmowaZlecenieConfig() != null && dto.getUmowaZlecenieConfig().getCostRateType() != null) {
                    if (dto.getUmowaZlecenieConfig().getCostRateType().getRate() != null) {
                        kosztyRateZlecenie = dto.getUmowaZlecenieConfig().getCostRateType().getRate();
                    } else if (dto.getUmowaZlecenieConfig().getCustomCostRate() != null) {
                        kosztyRateZlecenie = dto.getUmowaZlecenieConfig().getCustomCostRate();
                    }
                }
                BigDecimal kosztyZlecenie = podstawaZlecenie.multiply(kosztyRateZlecenie);
                return podstawaZlecenie.subtract(kosztyZlecenie).max(BigDecimal.ZERO);
            case B2B:
                if (dto.getB2bConfig() != null) {
                    BigDecimal zusB2b = dto.getB2bConfig().getZusAmount();
                    if (zusB2b == null) {
                        zusB2b = dto.getB2bConfig().getZusType().getDefaultAmount();
                    }
                    return brutto.subtract(zusB2b).max(BigDecimal.ZERO);
                }
                return brutto;
            case UMOWA_O_DZIELO:
                // Koszty z konfiguracji lub domyślne 20%
                BigDecimal kosztyRateDzielo = new BigDecimal("0.20");
                if (dto.getUmowaODzieloConfig() != null && dto.getUmowaODzieloConfig().getCostRateType() != null) {
                    if (dto.getUmowaODzieloConfig().getCostRateType().getRate() != null) {
                        kosztyRateDzielo = dto.getUmowaODzieloConfig().getCostRateType().getRate();
                    } else if (dto.getUmowaODzieloConfig().getCustomCostRate() != null) {
                        kosztyRateDzielo = dto.getUmowaODzieloConfig().getCustomCostRate();
                    }
                }
                return brutto.multiply(BigDecimal.ONE.subtract(kosztyRateDzielo));
            default:
                return brutto;
        }
    }

    private IncomeSourceDTO copyDto(IncomeSourceDTO original) {
        IncomeSourceDTO copy = new IncomeSourceDTO();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setIncomeType(original.getIncomeType());
        copy.setAmount(original.getAmount());
        copy.setAmountType(original.getAmountType());
        copy.setRateType(original.getRateType());
        copy.setHourlyRate(original.getHourlyRate());
        copy.setDefaultHoursPerMonth(original.getDefaultHoursPerMonth());
        copy.setActive(original.isActive());
        copy.setB2bConfig(original.getB2bConfig());
        copy.setUopConfig(original.getUopConfig());
        copy.setUmowaZlecenieConfig(original.getUmowaZlecenieConfig());
        copy.setUmowaODzieloConfig(original.getUmowaODzieloConfig());
        return copy;
    }
}
