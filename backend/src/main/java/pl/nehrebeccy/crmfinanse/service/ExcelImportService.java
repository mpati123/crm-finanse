package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.nehrebeccy.crmfinanse.model.Category;
import pl.nehrebeccy.crmfinanse.model.Expense;
import pl.nehrebeccy.crmfinanse.model.Income;
import pl.nehrebeccy.crmfinanse.repository.CategoryRepository;
import pl.nehrebeccy.crmfinanse.repository.ExpenseRepository;
import pl.nehrebeccy.crmfinanse.repository.IncomeRepository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExcelImportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;

    public void importFromExcel(MultipartFile file, String sheetName) throws IOException {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Arkusz nie znaleziony: " + sheetName);
            }

            importSheet(sheet);
        }
    }

    private void importSheet(Sheet sheet) {
        List<Expense> expenses = new ArrayList<>();
        List<Income> incomes = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().withDayOfMonth(1);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Kolumny A-C: Wydatki
            Cell nameCell = row.getCell(0);
            Cell amountCell = row.getCell(1);
            Cell statusCell = row.getCell(2);

            if (nameCell != null && amountCell != null) {
                String name = getCellStringValue(nameCell);
                BigDecimal amount = getCellNumericValue(amountCell);

                if (name != null && !name.isEmpty() && amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                    Expense.PaymentStatus status = parseStatus(statusCell);
                    Category category = getOrCreateCategory(name, Category.CategoryType.EXPENSE);

                    Expense expense = Expense.builder()
                            .name(name)
                            .amount(amount)
                            .category(category)
                            .date(currentDate)
                            .status(status)
                            .recurring(true)
                            .build();

                    expenses.add(expense);
                }
            }

            // Kolumny E-F: Przychody
            Cell incomeNameCell = row.getCell(4);
            Cell incomeAmountCell = row.getCell(5);

            if (incomeNameCell != null && incomeAmountCell != null) {
                String incomeName = getCellStringValue(incomeNameCell);
                BigDecimal incomeAmount = getCellNumericValue(incomeAmountCell);

                if (incomeName != null && !incomeName.isEmpty() && incomeAmount != null && incomeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    Category category = getOrCreateCategory(incomeName, Category.CategoryType.INCOME);

                    Income income = Income.builder()
                            .name(incomeName)
                            .amount(incomeAmount)
                            .category(category)
                            .date(currentDate)
                            .recurring(true)
                            .build();

                    incomes.add(income);
                }
            }
        }

        expenseRepository.saveAll(expenses);
        incomeRepository.saveAll(incomes);

        log.info("Zaimportowano {} wydatków i {} przychodów", expenses.size(), incomes.size());
    }

    private Category getOrCreateCategory(String name, Category.CategoryType type) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .type(type)
                        .color(generateColor(name))
                        .build()));
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return null;
    }

    private BigDecimal getCellNumericValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.FORMULA) {
            try {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Expense.PaymentStatus parseStatus(Cell cell) {
        if (cell == null) return Expense.PaymentStatus.PENDING;
        String value = getCellStringValue(cell);
        if (value == null) return Expense.PaymentStatus.PENDING;

        return switch (value.toLowerCase()) {
            case "t" -> Expense.PaymentStatus.PAID;
            case "n" -> Expense.PaymentStatus.PENDING;
            case "zost" -> Expense.PaymentStatus.REMAINING;
            default -> Expense.PaymentStatus.PENDING;
        };
    }

    private String generateColor(String name) {
        int hash = name.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        return String.format("#%02x%02x%02x", r % 200 + 55, g % 200 + 55, b % 200 + 55);
    }
}
