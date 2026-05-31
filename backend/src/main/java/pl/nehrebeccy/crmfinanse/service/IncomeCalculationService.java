package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.nehrebeccy.crmfinanse.dto.B2BConfigDTO;
import pl.nehrebeccy.crmfinanse.dto.IncomeSourceDTO;
import pl.nehrebeccy.crmfinanse.dto.UoPConfigDTO;
import pl.nehrebeccy.crmfinanse.model.TaxForm;
import pl.nehrebeccy.crmfinanse.model.ZUSType;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class IncomeCalculationService {

    // Stałe dla 2026 roku
    private static final BigDecimal ZUS_EMERYTALNE = new BigDecimal("0.0976");
    private static final BigDecimal ZUS_RENTOWE = new BigDecimal("0.015");
    private static final BigDecimal ZUS_CHOROBOWE = new BigDecimal("0.0245");
    private static final BigDecimal ZUS_PRACOWNIK_TOTAL = new BigDecimal("0.1371"); // 13.71%

    private static final BigDecimal SKLADKA_ZDROWOTNA = new BigDecimal("0.09"); // 9%
    private static final BigDecimal PIT_12 = new BigDecimal("0.12");
    private static final BigDecimal PIT_32 = new BigDecimal("0.32");
    private static final BigDecimal PIT_19 = new BigDecimal("0.19"); // liniowy

    private static final BigDecimal KWOTA_WOLNA = new BigDecimal("30000");
    private static final BigDecimal PROG_PODATKOWY = new BigDecimal("120000");
    private static final BigDecimal KOSZTY_UZYSKANIA = new BigDecimal("250"); // standardowe miesięczne

    public IncomeSourceDTO calculateNetIncome(IncomeSourceDTO dto) {
        BigDecimal netAmount;
        BigDecimal totalDeductions;
        BigDecimal grossAmount = dto.getAmount(); // domyślnie = amount

        switch (dto.getIncomeType()) {
            case UOP:
                netAmount = calculateUoPNet(dto);
                break;
            case B2B:
                netAmount = calculateB2BNet(dto);
                // Dla B2B oblicz kwotę brutto z VAT
                if (dto.getB2bConfig() != null && dto.getB2bConfig().isVatPayer()) {
                    Integer vatRate = dto.getB2bConfig().getVatRate();
                    if (vatRate == null) {
                        vatRate = 23; // domyślnie 23%
                    }
                    BigDecimal vatMultiplier = BigDecimal.ONE.add(
                        new BigDecimal(vatRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    );
                    grossAmount = dto.getAmount().multiply(vatMultiplier).setScale(2, RoundingMode.HALF_UP);
                }
                break;
            case UMOWA_ZLECENIE:
                netAmount = calculateUmowaZlecenieNet(dto);
                break;
            case UMOWA_O_DZIELO:
                netAmount = calculateUmowaODzieloNet(dto);
                break;
            case SWIADCZENIE:
            case CZYNSZ:
            case INNE:
            default:
                netAmount = dto.getAmount();
                break;
        }

        totalDeductions = dto.getAmount().subtract(netAmount);
        dto.setGrossAmount(grossAmount);
        dto.setNetAmount(netAmount);
        dto.setTotalDeductions(totalDeductions);
        return dto;
    }

    private BigDecimal calculateUoPNet(IncomeSourceDTO dto) {
        BigDecimal brutto = dto.getAmount();

        if (dto.getUopConfig() != null) {
            // Jeśli mamy ręcznie wpisane wartości, użyj ich
            if (dto.getUopConfig().getZusEmployee() != null &&
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
        }

        // Obliczenia automatyczne
        // 1. Składki ZUS pracownika
        BigDecimal zusEmployee = brutto.multiply(ZUS_PRACOWNIK_TOTAL);

        // 2. Podstawa składki zdrowotnej
        BigDecimal podstawaZdrowotna = brutto.subtract(zusEmployee);
        BigDecimal skladkaZdrowotna = podstawaZdrowotna.multiply(SKLADKA_ZDROWOTNA);

        // 3. Koszty uzyskania przychodu
        BigDecimal koszty = KOSZTY_UZYSKANIA;
        if (dto.getUopConfig() != null && dto.getUopConfig().isAuthorCosts()) {
            // 50% kosztów autorskich (max do limitu)
            BigDecimal authorCostsPercent = dto.getUopConfig().getAuthorCostsPercentage();
            if (authorCostsPercent == null) {
                authorCostsPercent = new BigDecimal("50");
            }
            BigDecimal authorCosts = podstawaZdrowotna.multiply(authorCostsPercent.divide(BigDecimal.valueOf(100)));
            koszty = authorCosts;
        }

        // 4. Podstawa opodatkowania
        BigDecimal podstawaOpodatkowania = podstawaZdrowotna.subtract(koszty);
        if (podstawaOpodatkowania.compareTo(BigDecimal.ZERO) < 0) {
            podstawaOpodatkowania = BigDecimal.ZERO;
        }

        // 5. Zaliczka na PIT (miesięczna, uproszczona - zakładamy 12%)
        BigDecimal kwotaWolnaMiesieczna = KWOTA_WOLNA.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal podatek = podstawaOpodatkowania.multiply(PIT_12).subtract(kwotaWolnaMiesieczna);
        if (podatek.compareTo(BigDecimal.ZERO) < 0) {
            podatek = BigDecimal.ZERO;
        }

        // 6. Netto
        BigDecimal netto = brutto
                .subtract(zusEmployee)
                .subtract(skladkaZdrowotna)
                .subtract(podatek);

        // 7. PPK jeśli uczestniczy
        if (dto.getUopConfig() != null && dto.getUopConfig().isPpk()) {
            BigDecimal ppkRate = dto.getUopConfig().getPpkRate();
            if (ppkRate == null) {
                ppkRate = new BigDecimal("2"); // domyślne 2%
            }
            BigDecimal ppk = brutto.multiply(ppkRate.divide(BigDecimal.valueOf(100)));
            netto = netto.subtract(ppk);
        }

        return netto.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateB2BNet(IncomeSourceDTO dto) {
        if (dto.getB2bConfig() == null) {
            return dto.getAmount();
        }

        // Kwota wprowadzona to przychód NETTO fakturowy (bez VAT)
        // czyli stawka × godziny lub kwota miesięczna netto
        BigDecimal przychod = dto.getAmount();

        // VAT jest osobno - nie wpływa na kalkulację netto dla przedsiębiorcy
        // VAT należny odprowadzamy do US, ale to nie zmniejsza naszego przychodu
        // Liczymy od przychodu netto fakturowego

        // 1. ZUS społeczny
        BigDecimal zus = dto.getB2bConfig().getZusAmount();
        if (zus == null) {
            zus = dto.getB2bConfig().getZusType().getDefaultAmount();
        }

        // 2. Dochód = Przychód - ZUS (koszty uzyskania = ZUS dla uproszczenia)
        BigDecimal dochod = przychod.subtract(zus);
        if (dochod.compareTo(BigDecimal.ZERO) < 0) {
            dochod = BigDecimal.ZERO;
        }

        // 3. Składka zdrowotna - zależy od formy opodatkowania
        BigDecimal zdrowotna = dto.getB2bConfig().getHealthInsurance();
        if (zdrowotna == null) {
            TaxForm taxForm = dto.getB2bConfig().getTaxForm();
            if (taxForm == TaxForm.LINIOWY) {
                // Dla liniowego 4.9% od dochodu (min. 314,10 zł w 2024)
                zdrowotna = dochod.multiply(new BigDecimal("0.049"));
                BigDecimal minZdrowotna = new BigDecimal("314.10");
                if (zdrowotna.compareTo(minZdrowotna) < 0) {
                    zdrowotna = minZdrowotna;
                }
            } else if (taxForm == TaxForm.RYCZALT) {
                // Ryczałt - składka zryczałtowana zależna od przychodu rocznego
                // Uproszczenie: ~420 zł miesięcznie dla średnich przychodów
                zdrowotna = new BigDecimal("420");
            } else {
                // Skala podatkowa - 9% od dochodu (min. 314,10 zł)
                zdrowotna = dochod.multiply(new BigDecimal("0.09"));
                BigDecimal minZdrowotna = new BigDecimal("314.10");
                if (zdrowotna.compareTo(minZdrowotna) < 0) {
                    zdrowotna = minZdrowotna;
                }
            }
        }
        if (zdrowotna.compareTo(BigDecimal.ZERO) < 0) {
            zdrowotna = BigDecimal.ZERO;
        }

        // 4. Podatek dochodowy
        BigDecimal podatek = dto.getB2bConfig().getIncomeTaxAdvance();
        if (podatek == null) {
            TaxForm taxForm = dto.getB2bConfig().getTaxForm();

            if (taxForm == TaxForm.LINIOWY) {
                // 19% od dochodu
                podatek = dochod.multiply(PIT_19);
            } else if (taxForm == TaxForm.RYCZALT) {
                // Ryczałt - procent od przychodu (bez odliczania kosztów)
                BigDecimal ryczaltRate = dto.getB2bConfig().getRyczaltRate();
                if (ryczaltRate == null) {
                    ryczaltRate = new BigDecimal("12"); // domyślnie 12% dla IT
                }
                podatek = przychod.multiply(ryczaltRate.divide(BigDecimal.valueOf(100)));
            } else {
                // Skala podatkowa - 12% od dochodu minus kwota wolna
                BigDecimal kwotaWolnaMiesieczna = KWOTA_WOLNA.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
                podatek = dochod.multiply(PIT_12).subtract(kwotaWolnaMiesieczna);
            }
        }
        if (podatek.compareTo(BigDecimal.ZERO) < 0) {
            podatek = BigDecimal.ZERO;
        }

        // 5. Netto = Przychód - ZUS - Zdrowotna - Podatek
        BigDecimal netto = przychod
                .subtract(zus)
                .subtract(zdrowotna)
                .subtract(podatek);

        return netto.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateUmowaZlecenieNet(IncomeSourceDTO dto) {
        BigDecimal brutto = dto.getAmount();

        // Składki ZUS - podobne do UoP
        BigDecimal zusEmployee = brutto.multiply(ZUS_PRACOWNIK_TOTAL);

        // Podstawa składki zdrowotnej
        BigDecimal podstawaZdrowotna = brutto.subtract(zusEmployee);
        BigDecimal skladkaZdrowotna = podstawaZdrowotna.multiply(SKLADKA_ZDROWOTNA);

        // Koszty uzyskania 20%
        BigDecimal koszty = brutto.multiply(new BigDecimal("0.20"));

        // Podstawa opodatkowania
        BigDecimal podstawaOpodatkowania = podstawaZdrowotna.subtract(koszty);
        if (podstawaOpodatkowania.compareTo(BigDecimal.ZERO) < 0) {
            podstawaOpodatkowania = BigDecimal.ZERO;
        }

        // Zaliczka na PIT 12%
        BigDecimal podatek = podstawaOpodatkowania.multiply(PIT_12);

        // Netto
        BigDecimal netto = brutto
                .subtract(zusEmployee)
                .subtract(skladkaZdrowotna)
                .subtract(podatek);

        return netto.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateUmowaODzieloNet(IncomeSourceDTO dto) {
        BigDecimal brutto = dto.getAmount();

        // Umowa o dzieło - bez ZUS
        // Koszty uzyskania 20% lub 50% dla prac twórczych
        BigDecimal kosztyRate = new BigDecimal("0.20");
        if (dto.getUopConfig() != null && dto.getUopConfig().isAuthorCosts()) {
            kosztyRate = new BigDecimal("0.50");
        }
        BigDecimal koszty = brutto.multiply(kosztyRate);

        // Podstawa opodatkowania
        BigDecimal podstawaOpodatkowania = brutto.subtract(koszty);

        // Zaliczka na PIT 12%
        BigDecimal podatek = podstawaOpodatkowania.multiply(PIT_12);

        // Netto
        BigDecimal netto = brutto.subtract(podatek);

        return netto.setScale(2, RoundingMode.HALF_UP);
    }
}
