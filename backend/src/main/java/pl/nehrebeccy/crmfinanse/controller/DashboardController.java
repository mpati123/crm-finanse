package pl.nehrebeccy.crmfinanse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nehrebeccy.crmfinanse.dto.DashboardDTO;
import pl.nehrebeccy.crmfinanse.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/{year}/{month}")
    public ResponseEntity<DashboardDTO> getDashboard(@PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(dashboardService.getDashboard(year, month));
    }
}
