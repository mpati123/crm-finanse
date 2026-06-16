package pl.nehrebeccy.crmfinanse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.nehrebeccy.crmfinanse.model.SavingsGoal;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByStatus(SavingsGoal.GoalStatus status);

    List<SavingsGoal> findByStatusIn(List<SavingsGoal.GoalStatus> statuses);

    List<SavingsGoal> findByStatusOrderByPriorityDescTargetDateAsc(SavingsGoal.GoalStatus status);

    List<SavingsGoal> findAllByOrderByPriorityDescTargetDateAsc();

    List<SavingsGoal> findAllByOrderByDisplayOrderAsc();
}
