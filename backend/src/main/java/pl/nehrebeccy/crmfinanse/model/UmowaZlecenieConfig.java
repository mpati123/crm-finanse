package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Konfiguracja dla Umowy Zlecenie.
 * Pozwala na konfigurację składek ZUS, kosztów uzyskania przychodu, podatku i PPK.
 */
@Entity
@Table(name = "umowa_zlecenie_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UmowaZlecenieConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Składki ZUS ===
    /**
     * Czy naliczać składki ZUS (em+rent+chor+zdr).
     * Domyślnie true - zleceniobiorca podlega ZUS.
     * False gdy ma etat gdzie indziej i nie chce płacić ZUS od zlecenia.
     */
    @Column(name = "with_zus")
    private boolean withZus = true;

    /**
     * Ręcznie wpisana kwota ZUS pracownika (opcjonalne).
     * Jeśli null, obliczana automatycznie.
     */
    @Column(name = "zus_employee", precision = 10, scale = 2)
    private BigDecimal zusEmployee;

    /**
     * Ręcznie wpisana składka zdrowotna (opcjonalne).
     * Jeśli null, obliczana automatycznie.
     */
    @Column(name = "health_insurance", precision = 10, scale = 2)
    private BigDecimal healthInsurance;

    // === Koszty uzyskania przychodu ===
    /**
     * Typ kosztów uzyskania przychodu.
     * STANDARD_20 - 20% przychodu
     * AUTHOR_50 - 50% przychodu (prace twórcze, limit 120k/rok)
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

    /**
     * Czy złożono PIT-2 dla tego zlecenia.
     * Jeśli true, stosowana jest kwota zmniejszająca podatek (300 zł/mies.).
     */
    @Column(name = "pit2_filed")
    private boolean pit2Filed;

    // === PPK ===
    /**
     * Czy uczestniczy w PPK.
     */
    @Column(name = "ppk")
    private boolean ppk;

    /**
     * Stawka PPK pracownika (domyślnie 2%).
     */
    @Column(name = "ppk_rate", precision = 5, scale = 2)
    private BigDecimal ppkRate;
}
