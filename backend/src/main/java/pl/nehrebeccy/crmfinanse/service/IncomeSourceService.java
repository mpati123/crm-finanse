package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.B2BConfigDTO;
import pl.nehrebeccy.crmfinanse.dto.IncomeSourceDTO;
import pl.nehrebeccy.crmfinanse.dto.UoPConfigDTO;
import pl.nehrebeccy.crmfinanse.model.*;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;
import pl.nehrebeccy.crmfinanse.repository.IncomeSourceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeSourceService {

    private final IncomeSourceRepository incomeSourceRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeCalculationService calculationService;

    public List<IncomeSourceDTO> getAllIncomeSources() {
        return incomeSourceRepository.findAll().stream()
                .map(this::toDTO)
                .map(calculationService::calculateNetIncome)
                .collect(Collectors.toList());
    }

    public List<IncomeSourceDTO> getActiveIncomeSources() {
        return incomeSourceRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .map(calculationService::calculateNetIncome)
                .collect(Collectors.toList());
    }

    public IncomeSourceDTO getIncomeSourceById(Long id) {
        return incomeSourceRepository.findById(id)
                .map(this::toDTO)
                .map(calculationService::calculateNetIncome)
                .orElseThrow(() -> new RuntimeException("Źródło przychodu nie znalezione: " + id));
    }

    public IncomeSourceDTO createIncomeSource(IncomeSourceDTO dto) {
        IncomeSource incomeSource = toEntity(dto);
        incomeSource = incomeSourceRepository.save(incomeSource);
        IncomeSourceDTO result = toDTO(incomeSource);
        return calculationService.calculateNetIncome(result);
    }

    public IncomeSourceDTO updateIncomeSource(Long id, IncomeSourceDTO dto) {
        IncomeSource incomeSource = incomeSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Źródło przychodu nie znalezione: " + id));

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

        incomeSource = incomeSourceRepository.save(incomeSource);
        IncomeSourceDTO result = toDTO(incomeSource);
        return calculationService.calculateNetIncome(result);
    }

    public void deleteIncomeSource(Long id) {
        incomeSourceRepository.deleteById(id);
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
}
