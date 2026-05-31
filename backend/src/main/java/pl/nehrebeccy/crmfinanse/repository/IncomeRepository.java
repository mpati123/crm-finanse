package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.Income;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Income> findByCategoryId(Long categoryId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT YEAR(i.date), MONTH(i.date), SUM(i.amount) FROM Income i GROUP BY YEAR(i.date), MONTH(i.date) ORDER BY YEAR(i.date), MONTH(i.date)")
    List<Object[]> sumAmountByMonth();
}
