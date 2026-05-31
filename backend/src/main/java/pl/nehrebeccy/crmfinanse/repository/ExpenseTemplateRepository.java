package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.ExpenseFrequency;
import pl.nehrebeccy.crmfinanse.model.ExpenseTemplate;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseTemplateRepository extends JpaRepository<ExpenseTemplate, Long> {

    List<ExpenseTemplate> findByActiveTrue();

    List<ExpenseTemplate> findByFrequency(ExpenseFrequency frequency);

    @Query("SELECT et FROM ExpenseTemplate et WHERE et.active = true " +
           "AND (et.startDate IS NULL OR et.startDate <= :date) " +
           "AND (et.endDate IS NULL OR et.endDate >= :date)")
    List<ExpenseTemplate> findActiveTemplatesForDate(@Param("date") LocalDate date);

    List<ExpenseTemplate> findByCategoryId(Long categoryId);
}
