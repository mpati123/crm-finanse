package pl.nehrebeccy.crmfinanse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.dto.BudgetDTO;
import pl.nehrebeccy.crmfinanse.service.BudgetService;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByMonth(@PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(budgetService.getBudgetsByMonth(year, month));
    }

    @GetMapping("/month/{year}/{month}/actuals")
    public ResponseEntity<List<BudgetDTO>> getBudgetsWithActuals(@PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(budgetService.getBudgetsWithActuals(year, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> createBudget(@Valid @RequestBody BudgetDTO budgetDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(budgetDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetDTO budgetDTO) {
        return ResponseEntity.ok(budgetService.updateBudget(id, budgetDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
