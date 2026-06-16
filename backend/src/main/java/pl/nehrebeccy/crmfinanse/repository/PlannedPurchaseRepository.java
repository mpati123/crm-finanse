package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.PlannedPurchase;

import java.util.List;

@Repository
public interface PlannedPurchaseRepository extends JpaRepository<PlannedPurchase, Long> {

    List<PlannedPurchase> findByPlannedYearAndPlannedMonth(Integer year, Integer month);

    List<PlannedPurchase> findByPlannedYear(Integer year);

    List<PlannedPurchase> findByStatus(PlannedPurchase.PurchaseStatus status);

    List<PlannedPurchase> findByPlannedYearAndPlannedMonthAndStatus(
            Integer year, Integer month, PlannedPurchase.PurchaseStatus status);

    List<PlannedPurchase> findByCategoryId(Long categoryId);
}
