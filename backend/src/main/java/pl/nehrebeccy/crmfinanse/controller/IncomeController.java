package pl.nehrebeccy.crmfinanse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.dto.IncomeDTO;
import pl.nehrebeccy.crmfinanse.service.IncomeService;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getAllIncomes() {
        return ResponseEntity.ok(incomeService.getAllIncomes());
    }

    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<IncomeDTO>> getIncomesByMonth(@PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(incomeService.getIncomesByMonth(year, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeDTO> getIncomeById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.getIncomeById(id));
    }

    @PostMapping
    public ResponseEntity<IncomeDTO> createIncome(@Valid @RequestBody IncomeDTO incomeDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeService.createIncome(incomeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeDTO> updateIncome(@PathVariable Long id, @Valid @RequestBody IncomeDTO incomeDTO) {
        return ResponseEntity.ok(incomeService.updateIncome(id, incomeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }
}
