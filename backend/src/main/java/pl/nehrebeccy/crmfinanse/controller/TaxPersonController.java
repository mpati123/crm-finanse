package pl.nehrebeccy.crmfinanse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.dto.TaxPersonDTO;
import pl.nehrebeccy.crmfinanse.model.CostType;
import pl.nehrebeccy.crmfinanse.service.TaxPersonService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tax-persons")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TaxPersonController {

    private final TaxPersonService taxPersonService;

    @GetMapping
    public ResponseEntity<List<TaxPersonDTO>> getAllTaxPersons() {
        return ResponseEntity.ok(taxPersonService.getAllTaxPersons());
    }

    @GetMapping("/active")
    public ResponseEntity<List<TaxPersonDTO>> getActiveTaxPersons() {
        return ResponseEntity.ok(taxPersonService.getActiveTaxPersons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaxPersonDTO> getTaxPersonById(@PathVariable Long id) {
        return ResponseEntity.ok(taxPersonService.getTaxPersonById(id));
    }

    @PostMapping
    public ResponseEntity<TaxPersonDTO> createTaxPerson(@Valid @RequestBody TaxPersonDTO dto) {
        return ResponseEntity.ok(taxPersonService.createTaxPerson(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxPersonDTO> updateTaxPerson(
            @PathVariable Long id,
            @Valid @RequestBody TaxPersonDTO dto) {
        return ResponseEntity.ok(taxPersonService.updateTaxPerson(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaxPerson(@PathVariable Long id) {
        taxPersonService.deleteTaxPerson(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-yearly/{year}")
    public ResponseEntity<Void> resetYearlyTracking(
            @PathVariable Long id,
            @PathVariable int year) {
        taxPersonService.resetYearlyTracking(id, year);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cost-types")
    public ResponseEntity<List<Map<String, Object>>> getCostTypes() {
        List<Map<String, Object>> types = Arrays.stream(CostType.values())
                .map(type -> Map.of(
                        "value", (Object) type.name(),
                        "label", (Object) type.getDisplayName(),
                        "amount", (Object) type.getAmount()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }
}
