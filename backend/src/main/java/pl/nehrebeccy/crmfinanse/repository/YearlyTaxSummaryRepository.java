package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.TaxPerson;
import pl.nehrebeccy.crmfinanse.model.YearlyTaxSummary;

import java.util.List;
import java.util.Optional;

@Repository
public interface YearlyTaxSummaryRepository extends JpaRepository<YearlyTaxSummary, Long> {

    List<YearlyTaxSummary> findByTaxPersonAndYear(TaxPerson taxPerson, int year);

    Optional<YearlyTaxSummary> findByTaxPersonAndYearAndMonth(TaxPerson taxPerson, int year, int month);

    List<YearlyTaxSummary> findByTaxPersonIdAndYearOrderByMonthAsc(Long taxPersonId, int year);

    void deleteByTaxPersonAndYear(TaxPerson taxPerson, int year);
}
