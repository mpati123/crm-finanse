package pl.nehrebeccy.crmfinanse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.dto.PlannedPurchaseDTO;
import pl.nehrebeccy.crmfinanse.model.PlannedPurchase;
import pl.nehrebeccy.crmfinanse.service.PlannedPurchaseService;

import java.util.List;

@RestController
@RequestMapping("/api/planned-purchases")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlannedPurchaseController {

    private final PlannedPurchaseService service;

    @GetMapping
    public ResponseEntity<List<PlannedPurchaseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlannedPurchaseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<PlannedPurchaseDTO>> getByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(service.getByYear(year));
    }

    @GetMapping("/year/{year}/month/{month}")
    public ResponseEntity<List<PlannedPurchaseDTO>> getByYearAndMonth(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        return ResponseEntity.ok(service.getByYearAndMonth(year, month));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PlannedPurchaseDTO>> getByStatus(
            @PathVariable PlannedPurchase.PurchaseStatus status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @PostMapping
    public ResponseEntity<PlannedPurchaseDTO> create(@RequestBody PlannedPurchaseDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlannedPurchaseDTO> update(
            @PathVariable Long id,
            @RequestBody PlannedPurchaseDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/mark-purchased")
    public ResponseEntity<PlannedPurchaseDTO> markAsPurchased(@PathVariable Long id) {
        return ResponseEntity.ok(service.markAsPurchased(id));
    }

    @PostMapping("/{id}/link-expense")
    public ResponseEntity<PlannedPurchaseDTO> linkExpense(@PathVariable Long id) {
        return ResponseEntity.ok(service.linkExpense(id));
    }

    @PostMapping("/fix-missing-expenses")
    public ResponseEntity<List<PlannedPurchaseDTO>> fixMissingExpenses() {
        List<PlannedPurchaseDTO> all = service.getAll();
        return ResponseEntity.ok(all.stream()
                .filter(p -> p.getExpenseId() == null)
                .map(p -> service.linkExpense(p.getId()))
                .toList());
    }
}
