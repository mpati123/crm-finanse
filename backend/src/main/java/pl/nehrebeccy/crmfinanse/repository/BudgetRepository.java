package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.Budget;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByYearAndMonth(Integer year, Integer month);

    Optional<Budget> findByYearAndMonthAndCategoryId(Integer year, Integer month, Long categoryId);

    List<Budget> findByYear(Integer year);
}
