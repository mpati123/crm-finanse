package pl.nehrebeccy.crmfinanse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.nehrebeccy.crmfinanse.service.ExcelImportService;

import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ImportController {

    private final ExcelImportService excelImportService;

    @PostMapping("/excel")
    public ResponseEntity<Map<String, String>> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sheet", defaultValue = "Arkusz90") String sheetName) {
        try {
            excelImportService.importFromExcel(file, sheetName);
            return ResponseEntity.ok(Map.of("message", "Import zakończony pomyślnie"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Błąd importu: " + e.getMessage()));
        }
    }
}
