package pl.nehrebeccy.crmfinanse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.service.RecurringTransactionService;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping("/generate/{year}/{month}")
    public ResponseEntity<RecurringTransactionService.GenerationResult> generateForMonth(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(recurringTransactionService.generateAllForMonth(year, month));
    }

    @PostMapping("/generate/incomes/{year}/{month}")
    public ResponseEntity<?> generateIncomesForMonth(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(recurringTransactionService.generateIncomesForMonth(year, month));
    }

    @PostMapping("/generate/expenses/{year}/{month}")
    public ResponseEntity<?> generateExpensesForMonth(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(recurringTransactionService.generateExpensesForMonth(year, month));
    }
}
