package pl.nehrebeccy.crmfinanse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "uop_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UoPConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zus_employee", precision = 10, scale = 2)
    private BigDecimal zusEmployee;

    @Column(name = "health_insurance", precision = 10, scale = 2)
    private BigDecimal healthInsurance;

    @Column(name = "income_tax", precision = 10, scale = 2)
    private BigDecimal incomeTax;

    @Column(name = "ppk")
    private boolean ppk;

    @Column(name = "ppk_rate", precision = 5, scale = 2)
    private BigDecimal ppkRate;

    @Column(name = "author_costs")
    private boolean authorCosts;

    @Column(name = "author_costs_percentage", precision = 5, scale = 2)
    private BigDecimal authorCostsPercentage;
}
