package pl.nehrebeccy.crmfinanse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.dto.ExpenseTemplateDTO;
import pl.nehrebeccy.crmfinanse.model.ExpenseFrequency;
import pl.nehrebeccy.crmfinanse.service.ExpenseTemplateService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expense-templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ExpenseTemplateController {

    private final ExpenseTemplateService expenseTemplateService;

    @GetMapping
    public ResponseEntity<List<ExpenseTemplateDTO>> getAllExpenseTemplates() {
        return ResponseEntity.ok(expenseTemplateService.getAllExpenseTemplates());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ExpenseTemplateDTO>> getActiveExpenseTemplates() {
        return ResponseEntity.ok(expenseTemplateService.getActiveExpenseTemplates());
    }

    @GetMapping("/active-for-date")
    public ResponseEntity<List<ExpenseTemplateDTO>> getActiveExpenseTemplatesForDate(
            @RequestParam(required = false) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return ResponseEntity.ok(expenseTemplateService.getActiveExpenseTemplatesForDate(date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseTemplateDTO> getExpenseTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseTemplateService.getExpenseTemplateById(id));
    }

    @PostMapping
    public ResponseEntity<ExpenseTemplateDTO> createExpenseTemplate(@Valid @RequestBody ExpenseTemplateDTO dto) {
        return ResponseEntity.ok(expenseTemplateService.createExpenseTemplate(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseTemplateDTO> updateExpenseTemplate(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseTemplateDTO dto) {
        return ResponseEntity.ok(expenseTemplateService.updateExpenseTemplate(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenseTemplate(@PathVariable Long id) {
        expenseTemplateService.deleteExpenseTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/frequencies")
    public ResponseEntity<List<Map<String, String>>> getFrequencies() {
        List<Map<String, String>> frequencies = Arrays.stream(ExpenseFrequency.values())
                .map(freq -> Map.of(
                        "value", freq.name(),
                        "label", freq.getDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(frequencies);
    }
}
