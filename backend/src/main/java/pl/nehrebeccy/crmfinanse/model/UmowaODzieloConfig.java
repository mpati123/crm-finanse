package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Konfiguracja dla Umowy o Dzieło.
 *
 * Umowa o dzieło:
 * - NIE podlega składkom ZUS (chyba że zawarta z własnym pracodawcą)
 * - NIE ma PPK
 * - NIE stosuje się kwoty zmniejszającej (PIT-2 nie dotyczy)
 * - Ma koszty uzyskania 20% lub 50% (prace twórcze)
 */
@Entity
@Table(name = "umowa_o_dzielo_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UmowaODzieloConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Koszty uzyskania przychodu ===
    /**
     * Typ kosztów uzyskania przychodu.
     * STANDARD_20 - 20% przychodu
     * AUTHOR_50 - 50% przychodu (prace twórcze, artystyczne, limit 120k/rok)
     * CUSTOM - niestandardowy procent
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "cost_rate_type")
    private CostRateType costRateType = CostRateType.STANDARD_20;

    /**
     * Niestandardowa stawka kosztów (dla CUSTOM).
     * Wartość w procentach, np. 0.25 = 25%
     */
    @Column(name = "custom_cost_rate", precision = 5, scale = 4)
    private BigDecimal customCostRate;

    // === Podatek ===
    /**
     * Ręcznie wpisana zaliczka na podatek (opcjonalne).
     * Jeśli null, obliczana automatycznie.
     */
    @Column(name = "income_tax", precision = 10, scale = 2)
    private BigDecimal incomeTax;

    // Uwaga: Umowa o dzieło NIE MA:
    // - składek ZUS (ani społecznych ani zdrowotnych) - chyba że z własnym pracodawcą
    // - PPK
    // - PIT-2 (kwota zmniejszająca nie dotyczy)
}
