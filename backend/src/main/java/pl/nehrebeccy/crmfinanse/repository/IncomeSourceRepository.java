package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.IncomeSource;
import pl.nehrebeccy.crmfinanse.model.IncomeType;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeSourceRepository extends JpaRepository<IncomeSource, Long> {

    List<IncomeSource> findByActiveTrue();

    List<IncomeSource> findByIncomeType(IncomeType incomeType);

    List<IncomeSource> findByPersonName(String personName);

    List<IncomeSource> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrEndDateIsNull(
            LocalDate date, LocalDate date2);

    List<IncomeSource> findByActiveTrueAndStartDateLessThanEqualAndEndDateIsNullOrEndDateGreaterThanEqual(
            LocalDate startDate, LocalDate endDate);
}
