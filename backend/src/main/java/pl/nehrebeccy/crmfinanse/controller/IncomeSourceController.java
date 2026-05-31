package pl.nehrebeccy.crmfinanse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.dto.IncomeSourceDTO;
import pl.nehrebeccy.crmfinanse.model.IncomeType;
import pl.nehrebeccy.crmfinanse.model.TaxForm;
import pl.nehrebeccy.crmfinanse.model.ZUSType;
import pl.nehrebeccy.crmfinanse.service.IncomeSourceService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/income-sources")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class IncomeSourceController {

    private final IncomeSourceService incomeSourceService;

    @GetMapping
    public ResponseEntity<List<IncomeSourceDTO>> getAllIncomeSources() {
        return ResponseEntity.ok(incomeSourceService.getAllIncomeSources());
    }

    @GetMapping("/active")
    public ResponseEntity<List<IncomeSourceDTO>> getActiveIncomeSources() {
        return ResponseEntity.ok(incomeSourceService.getActiveIncomeSources());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeSourceDTO> getIncomeSourceById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeSourceService.getIncomeSourceById(id));
    }

    @PostMapping
    public ResponseEntity<IncomeSourceDTO> createIncomeSource(@Valid @RequestBody IncomeSourceDTO dto) {
        return ResponseEntity.ok(incomeSourceService.createIncomeSource(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeSourceDTO> updateIncomeSource(
            @PathVariable Long id,
            @Valid @RequestBody IncomeSourceDTO dto) {
        return ResponseEntity.ok(incomeSourceService.updateIncomeSource(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncomeSource(@PathVariable Long id) {
        incomeSourceService.deleteIncomeSource(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getIncomeTypes() {
        List<Map<String, String>> types = Arrays.stream(IncomeType.values())
                .map(type -> Map.of(
                        "value", type.name(),
                        "label", type.getDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }

    @GetMapping("/tax-forms")
    public ResponseEntity<List<Map<String, String>>> getTaxForms() {
        List<Map<String, String>> forms = Arrays.stream(TaxForm.values())
                .map(form -> Map.of(
                        "value", form.name(),
                        "label", form.getDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(forms);
    }

    @GetMapping("/zus-types")
    public ResponseEntity<List<Map<String, Object>>> getZUSTypes() {
        List<Map<String, Object>> types = Arrays.stream(ZUSType.values())
                .map(type -> Map.of(
                        "value", (Object) type.name(),
                        "label", (Object) type.getDisplayName(),
                        "defaultAmount", (Object) type.getDefaultAmount()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }
}
