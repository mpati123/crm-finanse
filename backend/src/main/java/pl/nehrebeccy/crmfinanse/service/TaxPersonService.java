package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.TaxPersonDTO;
import pl.nehrebeccy.crmfinanse.model.TaxPerson;
import pl.nehrebeccy.crmfinanse.repository.TaxPersonRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaxPersonService {

    private final TaxPersonRepository taxPersonRepository;

    public List<TaxPersonDTO> getAllTaxPersons() {
        return taxPersonRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TaxPersonDTO> getActiveTaxPersons() {
        return taxPersonRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TaxPersonDTO getTaxPersonById(Long id) {
        return taxPersonRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Osoba podatkowa nie znaleziona: " + id));
    }

    public TaxPersonDTO createTaxPerson(TaxPersonDTO dto) {
        TaxPerson taxPerson = toEntity(dto);
        taxPerson.setTaxYear(LocalDate.now().getYear());
        taxPerson = taxPersonRepository.save(taxPerson);

        // Jeśli ustawiono małżonka, ustaw wzajemną relację
        if (dto.getSpouseId() != null) {
            setSpouseRelation(taxPerson, dto.getSpouseId());
        }

        return toDTO(taxPerson);
    }

    public TaxPersonDTO updateTaxPerson(Long id, TaxPersonDTO dto) {
        TaxPerson taxPerson = taxPersonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Osoba podatkowa nie znaleziona: " + id));

        Long previousSpouseId = taxPerson.getSpouse() != null ? taxPerson.getSpouse().getId() : null;

        taxPerson.setName(dto.getName());
        taxPerson.setPit2Filed(dto.isPit2Filed());
        taxPerson.setCostType(dto.getCostType());
        taxPerson.setJointTaxReturn(dto.isJointTaxReturn());
        taxPerson.setDefaultTaxForm(dto.getDefaultTaxForm());
        taxPerson.setDefaultZusType(dto.getDefaultZusType());
        taxPerson.setDefaultVatPayer(dto.isDefaultVatPayer());
        taxPerson.setActive(dto.isActive());

        taxPerson = taxPersonRepository.save(taxPerson);

        // Obsługa zmiany małżonka
        if (!java.util.Objects.equals(previousSpouseId, dto.getSpouseId())) {
            // Usuń poprzednią relację
            if (previousSpouseId != null) {
                TaxPerson previousSpouse = taxPersonRepository.findById(previousSpouseId).orElse(null);
                if (previousSpouse != null && previousSpouse.getSpouse() != null
                        && previousSpouse.getSpouse().getId().equals(id)) {
                    previousSpouse.setSpouse(null);
                    previousSpouse.setJointTaxReturn(false);
                    taxPersonRepository.save(previousSpouse);
                }
            }
            // Ustaw nową relację
            if (dto.getSpouseId() != null) {
                setSpouseRelation(taxPerson, dto.getSpouseId());
            } else {
                taxPerson.setSpouse(null);
                taxPersonRepository.save(taxPerson);
            }
        }

        return toDTO(taxPerson);
    }

    public void deleteTaxPerson(Long id) {
        TaxPerson taxPerson = taxPersonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Osoba podatkowa nie znaleziona: " + id));

        // Usuń relację małżonka
        if (taxPerson.getSpouse() != null) {
            TaxPerson spouse = taxPerson.getSpouse();
            spouse.setSpouse(null);
            spouse.setJointTaxReturn(false);
            taxPersonRepository.save(spouse);
        }

        taxPersonRepository.deleteById(id);
    }

    public void resetYearlyTracking(Long id, int year) {
        TaxPerson taxPerson = taxPersonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Osoba podatkowa nie znaleziona: " + id));

        taxPerson.setTaxYear(year);
        taxPerson.setCumulativeGrossIncome(BigDecimal.ZERO);
        taxPerson.setCumulativeTaxableIncome(BigDecimal.ZERO);
        taxPerson.setCumulativeZusPaid(BigDecimal.ZERO);
        taxPerson.setZusLimitReached(false);
        taxPerson.setSecondTaxBracket(false);

        taxPersonRepository.save(taxPerson);
    }

    private void setSpouseRelation(TaxPerson person, Long spouseId) {
        TaxPerson spouse = taxPersonRepository.findById(spouseId)
                .orElseThrow(() -> new RuntimeException("Małżonek nie znaleziony: " + spouseId));

        person.setSpouse(spouse);
        spouse.setSpouse(person);

        // Jeśli jeden ma jointTaxReturn, drugi też powinien
        if (person.isJointTaxReturn()) {
            spouse.setJointTaxReturn(true);
        }

        taxPersonRepository.save(person);
        taxPersonRepository.save(spouse);
    }

    private TaxPersonDTO toDTO(TaxPerson entity) {
        TaxPersonDTO.TaxPersonDTOBuilder builder = TaxPersonDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .pit2Filed(entity.isPit2Filed())
                .costType(entity.getCostType())
                .jointTaxReturn(entity.isJointTaxReturn())
                .taxYear(entity.getTaxYear())
                .cumulativeGrossIncome(entity.getCumulativeGrossIncome())
                .cumulativeTaxableIncome(entity.getCumulativeTaxableIncome())
                .cumulativeZusPaid(entity.getCumulativeZusPaid())
                .zusLimitReached(entity.isZusLimitReached())
                .secondTaxBracket(entity.isSecondTaxBracket())
                .defaultTaxForm(entity.getDefaultTaxForm())
                .defaultZusType(entity.getDefaultZusType())
                .defaultVatPayer(entity.isDefaultVatPayer())
                .active(entity.isActive());

        if (entity.getSpouse() != null) {
            builder.spouseId(entity.getSpouse().getId())
                   .spouseName(entity.getSpouse().getName());
        }

        // Oblicz postęp limitów
        BigDecimal zusLimit = PolishTaxConstants.ZUS_ANNUAL_LIMIT;
        BigDecimal taxThreshold = entity.isJointTaxReturn()
                ? PolishTaxConstants.TAX_THRESHOLD.multiply(BigDecimal.valueOf(2))
                : PolishTaxConstants.TAX_THRESHOLD;

        BigDecimal cumulativeGross = entity.getCumulativeGrossIncome() != null
                ? entity.getCumulativeGrossIncome() : BigDecimal.ZERO;
        BigDecimal cumulativeTaxable = entity.getCumulativeTaxableIncome() != null
                ? entity.getCumulativeTaxableIncome() : BigDecimal.ZERO;

        // ZUS progress
        BigDecimal zusProgress = zusLimit.compareTo(BigDecimal.ZERO) > 0
                ? cumulativeGross.multiply(BigDecimal.valueOf(100)).divide(zusLimit, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        builder.zusLimitProgress(zusProgress.min(BigDecimal.valueOf(100)));
        builder.remainingToZusLimit(zusLimit.subtract(cumulativeGross).max(BigDecimal.ZERO));

        // Tax threshold progress
        BigDecimal taxProgress = taxThreshold.compareTo(BigDecimal.ZERO) > 0
                ? cumulativeTaxable.multiply(BigDecimal.valueOf(100)).divide(taxThreshold, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        builder.taxThresholdProgress(taxProgress);
        builder.remainingToTaxThreshold(taxThreshold.subtract(cumulativeTaxable).max(BigDecimal.ZERO));

        return builder.build();
    }

    private TaxPerson toEntity(TaxPersonDTO dto) {
        return TaxPerson.builder()
                .name(dto.getName())
                .pit2Filed(dto.isPit2Filed())
                .costType(dto.getCostType() != null ? dto.getCostType() : pl.nehrebeccy.crmfinanse.model.CostType.STANDARD_250)
                .jointTaxReturn(dto.isJointTaxReturn())
                .taxYear(dto.getTaxYear() > 0 ? dto.getTaxYear() : LocalDate.now().getYear())
                .cumulativeGrossIncome(BigDecimal.ZERO)
                .cumulativeTaxableIncome(BigDecimal.ZERO)
                .cumulativeZusPaid(BigDecimal.ZERO)
                .zusLimitReached(false)
                .secondTaxBracket(false)
                .defaultTaxForm(dto.getDefaultTaxForm())
                .defaultZusType(dto.getDefaultZusType())
                .defaultVatPayer(dto.isDefaultVatPayer())
                .active(dto.isActive())
                .build();
    }
}
