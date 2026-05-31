package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.B2BConfigDTO;
import pl.nehrebeccy.crmfinanse.dto.IncomeDTO;
import pl.nehrebeccy.crmfinanse.dto.IncomeSourceDTO;
import pl.nehrebeccy.crmfinanse.dto.UoPConfigDTO;
import pl.nehrebeccy.crmfinanse.dto.UmowaZlecenieConfigDTO;
import pl.nehrebeccy.crmfinanse.dto.UmowaODzieloConfigDTO;
import pl.nehrebeccy.crmfinanse.model.*;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;
import pl.nehrebeccy.crmfinanse.repository.IncomeSourceRepository;
import pl.nehrebeccy.crmfinanse.repository.TaxPersonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeSourceService {

    private final IncomeSourceRepository incomeSourceRepository;
    private final CategoryRepository categoryRepository;
    private final TaxPersonRepository taxPersonRepository;
    private final IncomeCalculationService calculationService;
    private final IncomeService incomeService;

    public List<IncomeSourceDTO> getAllIncomeSources() {
        int currentMonth = LocalDate.now().getMonthValue();
        return incomeSourceRepository.findAll().stream()
                .map(this::toDTO)
                .map(dto -> calculateNetIncomeWithContext(dto, currentMonth))
                .collect(Collectors.toList());
    }

    public List<IncomeSourceDTO> getActiveIncomeSources() {
        int currentMonth = LocalDate.now().getMonthValue();
        return incomeSourceRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .map(dto -> calculateNetIncomeWithContext(dto, currentMonth))
                .collect(Collectors.toList());
    }

    public IncomeSourceDTO getIncomeSourceById(Long id) {
        int currentMonth = LocalDate.now().getMonthValue();
        return incomeSourceRepository.findById(id)
                .map(this::toDTO)
                .map(dto -> calculateNetIncomeWithContext(dto, currentMonth))
                .orElseThrow(() -> new RuntimeException("Zrodlo przychodu nie znalezione: " + id));
    }

    public IncomeSourceDTO createIncomeSource(IncomeSourceDTO dto) {
        IncomeSource incomeSource = toEntity(dto);
        incomeSource = incomeSourceRepository.save(incomeSource);
        IncomeSourceDTO result = toDTO(incomeSource);
        int currentMonth = LocalDate.now().getMonthValue();
        return calculateNetIncomeWithContext(result, currentMonth);
    }

    public IncomeSourceDTO updateIncomeSource(Long id, IncomeSourceDTO dto) {
        IncomeSource incomeSource = incomeSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zrodlo przychodu nie znalezione: " + id));

        incomeSource.setName(dto.getName());
        incomeSource.setPersonName(dto.getPersonName());
        incomeSource.setIncomeType(dto.getIncomeType());
        incomeSource.setAmount(dto.getAmount());
        incomeSource.setAmountType(dto.getAmountType() != null ? dto.getAmountType() : AmountType.GROSS);
        incomeSource.setRateType(dto.getRateType() != null ? dto.getRateType() : RateType.MONTHLY);
        incomeSource.setHourlyRate(dto.getHourlyRate());
        incomeSource.setDefaultHoursPerMonth(dto.getDefaultHoursPerMonth());
        incomeSource.setEmploymentFraction(dto.getEmploymentFraction());
        incomeSource.setStartDate(dto.getStartDate());
        incomeSource.setEndDate(dto.getEndDate());
        incomeSource.setPaymentDayOfMonth(dto.getPaymentDayOfMonth());
        incomeSource.setActive(dto.isActive());
        incomeSource.setNotes(dto.getNotes());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            incomeSource.setCategory(category);
        } else {
            incomeSource.setCategory(null);
        }

        // Aktualizacja konfiguracji B2B
        if (dto.getIncomeType() == IncomeType.B2B && dto.getB2bConfig() != null) {
            if (incomeSource.getB2bConfig() == null) {
                incomeSource.setB2bConfig(new B2BConfig());
            }
            updateB2BConfig(incomeSource.getB2bConfig(), dto.getB2bConfig());
        } else {
            incomeSource.setB2bConfig(null);
        }

        // Aktualizacja konfiguracji UoP
        if (dto.getIncomeType() == IncomeType.UOP && dto.getUopConfig() != null) {
            if (incomeSource.getUopConfig() == null) {
                incomeSource.setUopConfig(new UoPConfig());
            }
            updateUoPConfig(incomeSource.getUopConfig(), dto.getUopConfig());
        } else {
            incomeSource.setUopConfig(null);
        }

        // Aktualizacja konfiguracji Umowa zlecenie
        if (dto.getIncomeType() == IncomeType.UMOWA_ZLECENIE && dto.getUmowaZlecenieConfig() != null) {
            if (incomeSource.getUmowaZlecenieConfig() == null) {
                incomeSource.setUmowaZlecenieConfig(new UmowaZlecenieConfig());
            }
            updateUmowaZlecenieConfig(incomeSource.getUmowaZlecenieConfig(), dto.getUmowaZlecenieConfig());
        } else {
            incomeSource.setUmowaZlecenieConfig(null);
        }

        // Aktualizacja konfiguracji Umowa o dzielo
        if (dto.getIncomeType() == IncomeType.UMOWA_O_DZIELO && dto.getUmowaODzieloConfig() != null) {
            if (incomeSource.getUmowaODzieloConfig() == null) {
                incomeSource.setUmowaODzieloConfig(new UmowaODzieloConfig());
            }
            updateUmowaODzieloConfig(incomeSource.getUmowaODzieloConfig(), dto.getUmowaODzieloConfig());
        } else {
            incomeSource.setUmowaODzieloConfig(null);
        }

        // Aktualizacja osoby podatkowej
        if (dto.getTaxPersonId() != null) {
            TaxPerson taxPerson = taxPersonRepository.findById(dto.getTaxPersonId())
                    .orElseThrow(() -> new RuntimeException("Osoba podatkowa nie znaleziona: " + dto.getTaxPersonId()));
            incomeSource.setTaxPerson(taxPerson);
        } else {
            incomeSource.setTaxPerson(null);
        }

        incomeSource = incomeSourceRepository.save(incomeSource);
        IncomeSourceDTO result = toDTO(incomeSource);
        int currentMonth = LocalDate.now().getMonthValue();
        return calculateNetIncomeWithContext(result, currentMonth);
    }

    public void deleteIncomeSource(Long id) {
        incomeSourceRepository.deleteById(id);
    }

    public BigDecimal[] simulateYearlyNetIncome(Long id, Integer year) {
        IncomeSourceDTO dto = getIncomeSourceById(id);
        if (year != null) {
            // Pobierz rzeczywiste przychody dla tego źródła i roku
            List<IncomeDTO> actualIncomes = incomeService.getIncomesBySourceAndYear(id, year);
            return calculationService.simulateYearlyNetIncome(dto, year, actualIncomes);
        }
        return calculationService.simulateYearlyNetIncome(dto);
    }

    /**
     * Porównuje różne warianty rozliczeń w ramach tego samego typu zatrudnienia.
     * Dla UoP: porównuje różne warianty PPK (bez PPK, 2%, 4%)
     * Dla B2B: porównuje różne formy opodatkowania (skala, liniowy, ryczałt)
     * Dla umowy zlecenie: porównuje z ZUS i bez ZUS
     * Dla umowy o dzieło: porównuje różne procenty kosztów uzyskania przychodu
     */
    public Map<String, BigDecimal[]> compareTaxScenarios(Long id, Integer year) {
        IncomeSourceDTO dto = getIncomeSourceById(id);

        // Dla UoP - porównaj scenariusze PPK
        if (dto.getIncomeType() == IncomeType.UOP) {
            return compareUoPScenarios(dto, year);
        }

        // Dla B2B - porównaj formy opodatkowania
        if (dto.getIncomeType() == IncomeType.B2B) {
            return compareB2BScenarios(dto, year);
        }

        // Dla umowy zlecenie - porównaj z ZUS i bez ZUS
        if (dto.getIncomeType() == IncomeType.UMOWA_ZLECENIE) {
            return compareUmowaZlecenieScenarios(dto, year);
        }

        // Dla umowy o dzieło - porównaj różne procenty kosztów
        if (dto.getIncomeType() == IncomeType.UMOWA_O_DZIELO) {
            return compareUmowaODzieloScenarios(dto, year);
        }

        // Dla innych typów - zwróć tylko aktualną konfigurację
        Map<String, BigDecimal[]> result = new HashMap<>();
        if (year != null) {
            result.put("CURRENT", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            result.put("CURRENT", calculationService.simulateYearlyNetIncome(dto));
        }
        return result;
    }

    /**
     * Porównuje różne formy opodatkowania B2B: skala, liniowy 19%, ryczałt
     */
    private Map<String, BigDecimal[]> compareB2BScenarios(IncomeSourceDTO dto, Integer year) {
        Map<String, BigDecimal[]> scenarios = new HashMap<>();

        // Zapamiętaj oryginalne ustawienia
        B2BConfigDTO originalConfig = dto.getB2bConfig();
        TaxForm originalTaxForm = originalConfig != null ? originalConfig.getTaxForm() : TaxForm.SKALA;

        if (originalConfig == null) {
            dto.setB2bConfig(B2BConfigDTO.builder()
                    .zusType(ZUSType.PELNY)
                    .vatPayer(false)
                    .build());
        }

        // Scenariusz 1: Skala podatkowa (12% i 32%)
        dto.getB2bConfig().setTaxForm(TaxForm.SKALA);
        if (year != null) {
            scenarios.put("SKALA", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("SKALA", calculationService.simulateYearlyNetIncome(dto));
        }

        // Scenariusz 2: Podatek liniowy 19%
        dto.getB2bConfig().setTaxForm(TaxForm.LINIOWY);
        if (year != null) {
            scenarios.put("LINIOWY", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("LINIOWY", calculationService.simulateYearlyNetIncome(dto));
        }

        // Scenariusz 3: Ryczałt
        dto.getB2bConfig().setTaxForm(TaxForm.RYCZALT);
        if (year != null) {
            scenarios.put("RYCZALT", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("RYCZALT", calculationService.simulateYearlyNetIncome(dto));
        }

        // Przywróć oryginalne ustawienia
        dto.getB2bConfig().setTaxForm(originalTaxForm);

        return scenarios;
    }

    /**
     * Porównuje scenariusze dla umowy zlecenie: z ZUS, bez ZUS
     */
    private Map<String, BigDecimal[]> compareUmowaZlecenieScenarios(IncomeSourceDTO dto, Integer year) {
        Map<String, BigDecimal[]> scenarios = new HashMap<>();

        // Zapamiętaj oryginalne ustawienia
        UmowaZlecenieConfigDTO originalConfig = dto.getUmowaZlecenieConfig();
        boolean originalWithZus = originalConfig != null && originalConfig.isWithZus();

        if (originalConfig == null) {
            dto.setUmowaZlecenieConfig(UmowaZlecenieConfigDTO.builder()
                    .costRateType(CostRateType.STANDARD_20)
                    .pit2Filed(false)
                    .build());
        }

        // Scenariusz 1: Bez ZUS
        dto.getUmowaZlecenieConfig().setWithZus(false);
        if (year != null) {
            scenarios.put("BEZ_ZUS", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("BEZ_ZUS", calculationService.simulateYearlyNetIncome(dto));
        }

        // Scenariusz 2: Z ZUS
        dto.getUmowaZlecenieConfig().setWithZus(true);
        if (year != null) {
            scenarios.put("Z_ZUS", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("Z_ZUS", calculationService.simulateYearlyNetIncome(dto));
        }

        // Przywróć oryginalne ustawienia
        dto.getUmowaZlecenieConfig().setWithZus(originalWithZus);

        return scenarios;
    }

    /**
     * Porównuje scenariusze dla umowy o dzieło: różne procenty kosztów uzyskania przychodu
     */
    private Map<String, BigDecimal[]> compareUmowaODzieloScenarios(IncomeSourceDTO dto, Integer year) {
        Map<String, BigDecimal[]> scenarios = new HashMap<>();

        // Zapamiętaj oryginalne ustawienia
        UmowaODzieloConfigDTO originalConfig = dto.getUmowaODzieloConfig();
        CostRateType originalCostRate = originalConfig != null ? originalConfig.getCostRateType() : CostRateType.AUTHOR_50;

        if (originalConfig == null) {
            dto.setUmowaODzieloConfig(UmowaODzieloConfigDTO.builder()
                    .costRateType(CostRateType.AUTHOR_50)
                    .build());
        }

        // Scenariusz 1: 20% kosztów
        dto.getUmowaODzieloConfig().setCostRateType(CostRateType.STANDARD_20);
        if (year != null) {
            scenarios.put("KOSZTY_20%", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("KOSZTY_20%", calculationService.simulateYearlyNetIncome(dto));
        }

        // Scenariusz 2: 50% kosztów (typowe dla prac twórczych)
        dto.getUmowaODzieloConfig().setCostRateType(CostRateType.AUTHOR_50);
        if (year != null) {
            scenarios.put("KOSZTY_50%", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("KOSZTY_50%", calculationService.simulateYearlyNetIncome(dto));
        }

        // Przywróć oryginalne ustawienia
        dto.getUmowaODzieloConfig().setCostRateType(originalCostRate);

        return scenarios;
    }

    /**
     * Porównuje scenariusze PPK dla umowy o pracę.
     * Zwraca mapę z kluczami: "BEZ_PPK", "PPK_2%", "PPK_4%"
     */
    private Map<String, BigDecimal[]> compareUoPScenarios(IncomeSourceDTO dto, Integer year) {
        Map<String, BigDecimal[]> scenarios = new HashMap<>();

        // Zapamiętaj oryginalne ustawienia PPK
        UoPConfigDTO originalConfig = dto.getUopConfig();
        boolean originalPpk = false;
        BigDecimal originalPpkRate = BigDecimal.ZERO;

        if (originalConfig != null) {
            originalPpk = originalConfig.isPpk();
            originalPpkRate = originalConfig.getPpkRate();
        } else {
            // Jeśli nie ma konfiguracji, utwórz pustą
            dto.setUopConfig(UoPConfigDTO.builder().build());
        }

        // Scenariusz 1: BEZ PPK
        dto.getUopConfig().setPpk(false);
        dto.getUopConfig().setPpkRate(BigDecimal.ZERO);
        if (year != null) {
            scenarios.put("BEZ_PPK", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("BEZ_PPK", calculationService.simulateYearlyNetIncome(dto));
        }

        // Scenariusz 2: PPK 2% (podstawowa wpłata pracownika)
        dto.getUopConfig().setPpk(true);
        dto.getUopConfig().setPpkRate(new BigDecimal("2.0"));
        if (year != null) {
            scenarios.put("PPK_2%", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("PPK_2%", calculationService.simulateYearlyNetIncome(dto));
        }

        // Scenariusz 3: PPK 4% (maksymalna wpłata pracownika: 2% podstawowa + 2% dobrowolna)
        dto.getUopConfig().setPpk(true);
        dto.getUopConfig().setPpkRate(new BigDecimal("4.0"));
        if (year != null) {
            scenarios.put("PPK_4%", calculationService.simulateYearlyNetIncome(dto, year));
        } else {
            scenarios.put("PPK_4%", calculationService.simulateYearlyNetIncome(dto));
        }

        // Przywróć oryginalne ustawienia
        if (originalConfig != null) {
            dto.getUopConfig().setPpk(originalPpk);
            dto.getUopConfig().setPpkRate(originalPpkRate);
        }

        return scenarios;
    }

    public IncomeSourceDTO calculateNetIncomeForMonth(Long id, int month) {
        IncomeSource entity = incomeSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zrodlo przychodu nie znalezione: " + id));
        IncomeSourceDTO dto = toDTO(entity);
        return calculateNetIncomeWithContext(dto, month);
    }

    private IncomeSourceDTO calculateNetIncomeWithContext(IncomeSourceDTO dto, int month) {
        BigDecimal cumulativeGross = BigDecimal.ZERO;
        BigDecimal cumulativeIncome = BigDecimal.ZERO;
        for (int m = 1; m < month; m++) {
            cumulativeGross = cumulativeGross.add(dto.getAmount());
            cumulativeIncome = cumulativeIncome.add(calculateMonthlyIncome(dto));
        }
        IncomeCalculationService.YearlyContext context = new IncomeCalculationService.YearlyContext(
            month, cumulativeGross, cumulativeIncome
        );
        return calculationService.calculateNetIncome(dto, context);
    }

    private BigDecimal calculateMonthlyIncome(IncomeSourceDTO dto) {
        BigDecimal brutto = dto.getAmount();
        switch (dto.getIncomeType()) {
            case UOP:
            case UMOWA_ZLECENIE:
                BigDecimal zus = brutto.multiply(PolishTaxConstants.ZUS_TOTAL_EMPLOYEE);
                BigDecimal podstawa = brutto.subtract(zus);
                return podstawa.subtract(PolishTaxConstants.STANDARD_COSTS).max(BigDecimal.ZERO);
            case B2B:
                if (dto.getB2bConfig() != null) {
                    BigDecimal zusB2b = dto.getB2bConfig().getZusAmount();
                    if (zusB2b == null) {
                        zusB2b = dto.getB2bConfig().getZusType().getDefaultAmount();
                    }
                    return brutto.subtract(zusB2b).max(BigDecimal.ZERO);
                }
                return brutto;
            default:
                return brutto;
        }
    }

    private IncomeSourceDTO toDTO(IncomeSource entity) {
        IncomeSourceDTO.IncomeSourceDTOBuilder builder = IncomeSourceDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .personName(entity.getPersonName())
                .incomeType(entity.getIncomeType())
                .amount(entity.getAmount())
                .amountType(entity.getAmountType())
                .rateType(entity.getRateType())
                .hourlyRate(entity.getHourlyRate())
                .defaultHoursPerMonth(entity.getDefaultHoursPerMonth())
                .employmentFraction(entity.getEmploymentFraction())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .paymentDayOfMonth(entity.getPaymentDayOfMonth())
                .active(entity.isActive())
                .notes(entity.getNotes());

        if (entity.getCategory() != null) {
            builder.categoryId(entity.getCategory().getId())
                   .categoryName(entity.getCategory().getName());
        }

        if (entity.getB2bConfig() != null) {
            builder.b2bConfig(toB2BConfigDTO(entity.getB2bConfig()));
        }

        if (entity.getUopConfig() != null) {
            builder.uopConfig(toUoPConfigDTO(entity.getUopConfig()));
        }

        if (entity.getTaxPerson() != null) {
            builder.taxPersonId(entity.getTaxPerson().getId())
                   .taxPersonName(entity.getTaxPerson().getName());
        }

        if (entity.getUmowaZlecenieConfig() != null) {
            builder.umowaZlecenieConfig(toUmowaZlecenieConfigDTO(entity.getUmowaZlecenieConfig()));
        }

        if (entity.getUmowaODzieloConfig() != null) {
            builder.umowaODzieloConfig(toUmowaODzieloConfigDTO(entity.getUmowaODzieloConfig()));
        }

        return builder.build();
    }

    private IncomeSource toEntity(IncomeSourceDTO dto) {
        IncomeSource.IncomeSourceBuilder builder = IncomeSource.builder()
                .name(dto.getName())
                .personName(dto.getPersonName())
                .incomeType(dto.getIncomeType())
                .amount(dto.getAmount())
                .amountType(dto.getAmountType() != null ? dto.getAmountType() : AmountType.GROSS)
                .rateType(dto.getRateType() != null ? dto.getRateType() : RateType.MONTHLY)
                .hourlyRate(dto.getHourlyRate())
                .defaultHoursPerMonth(dto.getDefaultHoursPerMonth())
                .employmentFraction(dto.getEmploymentFraction())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .paymentDayOfMonth(dto.getPaymentDayOfMonth())
                .active(dto.isActive())
                .notes(dto.getNotes());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            builder.category(category);
        }

        if (dto.getIncomeType() == IncomeType.B2B && dto.getB2bConfig() != null) {
            builder.b2bConfig(toB2BConfigEntity(dto.getB2bConfig()));
        }

        if (dto.getIncomeType() == IncomeType.UOP && dto.getUopConfig() != null) {
            builder.uopConfig(toUoPConfigEntity(dto.getUopConfig()));
        }

        if (dto.getIncomeType() == IncomeType.UMOWA_ZLECENIE && dto.getUmowaZlecenieConfig() != null) {
            builder.umowaZlecenieConfig(toUmowaZlecenieConfigEntity(dto.getUmowaZlecenieConfig()));
        }

        if (dto.getIncomeType() == IncomeType.UMOWA_O_DZIELO && dto.getUmowaODzieloConfig() != null) {
            builder.umowaODzieloConfig(toUmowaODzieloConfigEntity(dto.getUmowaODzieloConfig()));
        }

        if (dto.getTaxPersonId() != null) {
            TaxPerson taxPerson = taxPersonRepository.findById(dto.getTaxPersonId())
                    .orElseThrow(() -> new RuntimeException("Osoba podatkowa nie znaleziona: " + dto.getTaxPersonId()));
            builder.taxPerson(taxPerson);
        }

        return builder.build();
    }

    private B2BConfigDTO toB2BConfigDTO(B2BConfig entity) {
        return B2BConfigDTO.builder()
                .id(entity.getId())
                .taxForm(entity.getTaxForm())
                .zusType(entity.getZusType())
                .vatPayer(entity.isVatPayer())
                .vatRate(entity.getVatRate())
                .zusAmount(entity.getZusAmount())
                .healthInsurance(entity.getHealthInsurance())
                .incomeTaxAdvance(entity.getIncomeTaxAdvance())
                .ryczaltRate(entity.getRyczaltRate())
                .build();
    }

    private B2BConfig toB2BConfigEntity(B2BConfigDTO dto) {
        return B2BConfig.builder()
                .taxForm(dto.getTaxForm())
                .zusType(dto.getZusType())
                .vatPayer(dto.isVatPayer())
                .vatRate(dto.getVatRate())
                .zusAmount(dto.getZusAmount())
                .healthInsurance(dto.getHealthInsurance())
                .incomeTaxAdvance(dto.getIncomeTaxAdvance())
                .ryczaltRate(dto.getRyczaltRate())
                .build();
    }

    private void updateB2BConfig(B2BConfig entity, B2BConfigDTO dto) {
        entity.setTaxForm(dto.getTaxForm());
        entity.setZusType(dto.getZusType());
        entity.setVatPayer(dto.isVatPayer());
        entity.setVatRate(dto.getVatRate());
        entity.setZusAmount(dto.getZusAmount());
        entity.setHealthInsurance(dto.getHealthInsurance());
        entity.setIncomeTaxAdvance(dto.getIncomeTaxAdvance());
        entity.setRyczaltRate(dto.getRyczaltRate());
    }

    private UoPConfigDTO toUoPConfigDTO(UoPConfig entity) {
        return UoPConfigDTO.builder()
                .id(entity.getId())
                .zusEmployee(entity.getZusEmployee())
                .healthInsurance(entity.getHealthInsurance())
                .incomeTax(entity.getIncomeTax())
                .ppk(entity.isPpk())
                .ppkRate(entity.getPpkRate())
                .authorCosts(entity.isAuthorCosts())
                .authorCostsPercentage(entity.getAuthorCostsPercentage())
                .build();
    }

    private UoPConfig toUoPConfigEntity(UoPConfigDTO dto) {
        return UoPConfig.builder()
                .zusEmployee(dto.getZusEmployee())
                .healthInsurance(dto.getHealthInsurance())
                .incomeTax(dto.getIncomeTax())
                .ppk(dto.isPpk())
                .ppkRate(dto.getPpkRate())
                .authorCosts(dto.isAuthorCosts())
                .authorCostsPercentage(dto.getAuthorCostsPercentage())
                .build();
    }

    private void updateUoPConfig(UoPConfig entity, UoPConfigDTO dto) {
        entity.setZusEmployee(dto.getZusEmployee());
        entity.setHealthInsurance(dto.getHealthInsurance());
        entity.setIncomeTax(dto.getIncomeTax());
        entity.setPpk(dto.isPpk());
        entity.setPpkRate(dto.getPpkRate());
        entity.setAuthorCosts(dto.isAuthorCosts());
        entity.setAuthorCostsPercentage(dto.getAuthorCostsPercentage());
    }

    private UmowaZlecenieConfigDTO toUmowaZlecenieConfigDTO(UmowaZlecenieConfig entity) {
        return UmowaZlecenieConfigDTO.builder()
                .id(entity.getId())
                .withZus(entity.isWithZus())
                .zusEmployee(entity.getZusEmployee())
                .healthInsurance(entity.getHealthInsurance())
                .costRateType(entity.getCostRateType())
                .customCostRate(entity.getCustomCostRate())
                .incomeTax(entity.getIncomeTax())
                .pit2Filed(entity.isPit2Filed())
                .ppk(entity.isPpk())
                .ppkRate(entity.getPpkRate())
                .build();
    }

    private UmowaZlecenieConfig toUmowaZlecenieConfigEntity(UmowaZlecenieConfigDTO dto) {
        return UmowaZlecenieConfig.builder()
                .withZus(dto.isWithZus())
                .zusEmployee(dto.getZusEmployee())
                .healthInsurance(dto.getHealthInsurance())
                .costRateType(dto.getCostRateType())
                .customCostRate(dto.getCustomCostRate())
                .incomeTax(dto.getIncomeTax())
                .pit2Filed(dto.isPit2Filed())
                .ppk(dto.isPpk())
                .ppkRate(dto.getPpkRate())
                .build();
    }

    private void updateUmowaZlecenieConfig(UmowaZlecenieConfig entity, UmowaZlecenieConfigDTO dto) {
        entity.setWithZus(dto.isWithZus());
        entity.setZusEmployee(dto.getZusEmployee());
        entity.setHealthInsurance(dto.getHealthInsurance());
        entity.setCostRateType(dto.getCostRateType());
        entity.setCustomCostRate(dto.getCustomCostRate());
        entity.setIncomeTax(dto.getIncomeTax());
        entity.setPit2Filed(dto.isPit2Filed());
        entity.setPpk(dto.isPpk());
        entity.setPpkRate(dto.getPpkRate());
    }

    private UmowaODzieloConfigDTO toUmowaODzieloConfigDTO(UmowaODzieloConfig entity) {
        return UmowaODzieloConfigDTO.builder()
                .id(entity.getId())
                .costRateType(entity.getCostRateType())
                .customCostRate(entity.getCustomCostRate())
                .incomeTax(entity.getIncomeTax())
                .build();
    }

    private UmowaODzieloConfig toUmowaODzieloConfigEntity(UmowaODzieloConfigDTO dto) {
        return UmowaODzieloConfig.builder()
                .costRateType(dto.getCostRateType())
                .customCostRate(dto.getCustomCostRate())
                .incomeTax(dto.getIncomeTax())
                .build();
    }

    private void updateUmowaODzieloConfig(UmowaODzieloConfig entity, UmowaODzieloConfigDTO dto) {
        entity.setCostRateType(dto.getCostRateType());
        entity.setCustomCostRate(dto.getCustomCostRate());
        entity.setIncomeTax(dto.getIncomeTax());
    }
}
