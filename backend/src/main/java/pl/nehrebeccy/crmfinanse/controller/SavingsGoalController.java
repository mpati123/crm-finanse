package pl.nehrebeccy.crmfinanse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.dto.SavingsGoalDTO;
import pl.nehrebeccy.crmfinanse.service.SavingsGoalService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/savings-goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SavingsGoalController {

    private final SavingsGoalService service;

    @GetMapping
    public ResponseEntity<List<SavingsGoalDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SavingsGoalDTO>> getActive() {
        return ResponseEntity.ok(service.getActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<SavingsGoalDTO> create(@RequestBody SavingsGoalDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalDTO> update(
            @PathVariable Long id,
            @RequestBody SavingsGoalDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<SavingsGoalDTO> addContribution(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        return ResponseEntity.ok(service.addContribution(id, amount));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<SavingsGoalDTO> withdrawAmount(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        return ResponseEntity.ok(service.withdrawAmount(id, amount));
    }
}
