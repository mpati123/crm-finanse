package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.TaxPerson;

import java.util.List;

@Repository
public interface TaxPersonRepository extends JpaRepository<TaxPerson, Long> {

    List<TaxPerson> findByActiveTrue();

    List<TaxPerson> findByTaxYear(int taxYear);

    List<TaxPerson> findByActiveTrueOrderByNameAsc();
}
