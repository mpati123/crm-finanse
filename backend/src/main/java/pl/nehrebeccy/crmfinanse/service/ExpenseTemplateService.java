package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.ExpenseTemplateDTO;
import pl.nehrebeccy.crmfinanse.model.Category;
import pl.nehrebeccy.crmfinanse.model.ExpenseTemplate;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;
import pl.nehrebeccy.crmfinanse.repository.ExpenseTemplateRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseTemplateService {

    private final ExpenseTemplateRepository expenseTemplateRepository;
    private final CategoryRepository categoryRepository;

    public List<ExpenseTemplateDTO> getAllExpenseTemplates() {
        return expenseTemplateRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseTemplateDTO> getActiveExpenseTemplates() {
        return expenseTemplateRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseTemplateDTO> getActiveExpenseTemplatesForDate(LocalDate date) {
        return expenseTemplateRepository.findActiveTemplatesForDate(date).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExpenseTemplateDTO getExpenseTemplateById(Long id) {
        return expenseTemplateRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Szablon wydatku nie znaleziony: " + id));
    }

    public ExpenseTemplateDTO createExpenseTemplate(ExpenseTemplateDTO dto) {
        ExpenseTemplate template = toEntity(dto);
        template = expenseTemplateRepository.save(template);
        return toDTO(template);
    }

    public ExpenseTemplateDTO updateExpenseTemplate(Long id, ExpenseTemplateDTO dto) {
        ExpenseTemplate template = expenseTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Szablon wydatku nie znaleziony: " + id));

        template.setName(dto.getName());
        template.setAmount(dto.getAmount());
        template.setStartDate(dto.getStartDate());
        template.setEndDate(dto.getEndDate());
        template.setDayOfMonth(dto.getDayOfMonth());
        template.setFrequency(dto.getFrequency());
        template.setActive(dto.isActive());
        template.setNotes(dto.getNotes());
        template.setAutoPay(dto.isAutoPay());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            template.setCategory(category);
        } else {
            template.setCategory(null);
        }

        template = expenseTemplateRepository.save(template);
        return toDTO(template);
    }

    public void deleteExpenseTemplate(Long id) {
        expenseTemplateRepository.deleteById(id);
    }

    private ExpenseTemplateDTO toDTO(ExpenseTemplate entity) {
        ExpenseTemplateDTO.ExpenseTemplateDTOBuilder builder = ExpenseTemplateDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .amount(entity.getAmount())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .dayOfMonth(entity.getDayOfMonth())
                .frequency(entity.getFrequency())
                .active(entity.isActive())
                .notes(entity.getNotes())
                .autoPay(entity.isAutoPay());

        if (entity.getCategory() != null) {
            builder.categoryId(entity.getCategory().getId())
                   .categoryName(entity.getCategory().getName());
        }

        return builder.build();
    }

    private ExpenseTemplate toEntity(ExpenseTemplateDTO dto) {
        ExpenseTemplate.ExpenseTemplateBuilder builder = ExpenseTemplate.builder()
                .name(dto.getName())
                .amount(dto.getAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .dayOfMonth(dto.getDayOfMonth())
                .frequency(dto.getFrequency())
                .active(dto.isActive())
                .notes(dto.getNotes())
                .autoPay(dto.isAutoPay());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategoria nie znaleziona: " + dto.getCategoryId()));
            builder.category(category);
        }

        return builder.build();
    }
}
