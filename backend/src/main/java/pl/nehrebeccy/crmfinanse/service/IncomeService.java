package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.IncomeDTO;
import pl.nehrebeccy.crmfinanse.model.Category;
import pl.nehrebeccy.crmfinanse.model.Income;
import pl.nehrebeccy.crmfinanse.model.TaxPerson;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;
import pl.nehrebeccy.crmfinanse.repository.IncomeRepository;
import pl.nehrebeccy.crmfinanse.repository.TaxPersonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final TaxPersonRepository taxPersonRepository;

    public List<IncomeDTO> getAllIncomes() {
        return incomeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<IncomeDTO> getIncomesByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return incomeRepository.findByDateBetween(startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public IncomeDTO getIncomeById(Long id) {
        return incomeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Przychód nie znaleziony: " + id));
    }

    public IncomeDTO createIncome(IncomeDTO dto) {
        Income income = toEntity(dto);
        income = incomeRepository.save(income);
        return toDTO(income);
    }

    public IncomeDTO updateIncome(Long id, IncomeDTO dto) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Przychód nie znaleziony: " + id));

        income.setName(dto.getName());
        income.setAmount(dto.getAmount());
        income.setDate(dto.getDate());
        income.setNotes(dto.getNotes());
        income.setRecurring(dto.isRecurring());
        income.setEstimated(dto.getEstimated());
        income.setNetAmount(dto.getNetAmount());
        income.setActualAmount(dto.getActualAmount());
        income.setActualHours(dto.getActualHours());
        income.setOvertimeHours100(dto.getOvertimeHours100());
        income.setOvertimeHours150(dto.getOvertimeHours150());
        income.setOvertimeHours200(dto.getOvertimeHours200());
        income.setIncomeSourceId(dto.getIncomeSourceId());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            income.setCategory(category);
        }

        if (dto.getTaxPersonId() != null) {
            TaxPerson taxPerson = taxPersonRepository.findById(dto.getTaxPersonId())
                    .orElseThrow(() -> new RuntimeException("Osoba podatkowa nie znaleziona: " + dto.getTaxPersonId()));
            income.setTaxPerson(taxPerson);
        } else {
            income.setTaxPerson(null);
        }

        return toDTO(incomeRepository.save(income));
    }

    public void deleteIncome(Long id) {
        incomeRepository.deleteById(id);
    }

    public BigDecimal getTotalIncomeByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        BigDecimal total = incomeRepository.sumAmountByDateBetween(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    private IncomeDTO toDTO(Income income) {
        return IncomeDTO.builder()
                .id(income.getId())
                .name(income.getName())
                .amount(income.getAmount())
                .categoryId(income.getCategory() != null ? income.getCategory().getId() : null)
                .categoryName(income.getCategory() != null ? income.getCategory().getName() : null)
                .date(income.getDate())
                .notes(income.getNotes())
                .recurring(income.isRecurring())
                .estimated(income.getEstimated())
                .netAmount(income.getNetAmount())
                .actualAmount(income.getActualAmount())
                .actualHours(income.getActualHours())
                .overtimeHours100(income.getOvertimeHours100())
                .overtimeHours150(income.getOvertimeHours150())
                .overtimeHours200(income.getOvertimeHours200())
                .incomeSourceId(income.getIncomeSourceId())
                .taxPersonId(income.getTaxPerson() != null ? income.getTaxPerson().getId() : null)
                .taxPersonName(income.getTaxPerson() != null ? income.getTaxPerson().getName() : null)
                .build();
    }

    private Income toEntity(IncomeDTO dto) {
        Income income = Income.builder()
                .name(dto.getName())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .notes(dto.getNotes())
                .recurring(dto.isRecurring())
                .estimated(dto.getEstimated())
                .netAmount(dto.getNetAmount())
                .actualAmount(dto.getActualAmount())
                .actualHours(dto.getActualHours())
                .overtimeHours100(dto.getOvertimeHours100())
                .overtimeHours150(dto.getOvertimeHours150())
                .overtimeHours200(dto.getOvertimeHours200())
                .incomeSourceId(dto.getIncomeSourceId())
                .build();

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            income.setCategory(category);
        }

        if (dto.getTaxPersonId() != null) {
            TaxPerson taxPerson = taxPersonRepository.findById(dto.getTaxPersonId())
                    .orElseThrow(() -> new RuntimeException("Osoba podatkowa nie znaleziona: " + dto.getTaxPersonId()));
            income.setTaxPerson(taxPerson);
        }

        return income;
    }

    public List<IncomeDTO> getIncomesBySourceAndYear(Long incomeSourceId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return incomeRepository.findByIncomeSourceIdAndDateBetween(incomeSourceId, startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

