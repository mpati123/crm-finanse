package pl.nehrebeccy.crmfinanse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nehrebeccy.crmfinanse.dto.SavingsGoalDTO;
import pl.nehrebeccy.crmfinanse.model.SavingsGoal;
import pl.nehrebeccy.crmfinanse.repository.SavingsGoalRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository repository;

    public List<SavingsGoalDTO> getAll() {
        return repository.findAllByOrderByPriorityDescTargetDateAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SavingsGoalDTO> getActive() {
        return repository.findByStatus(SavingsGoal.GoalStatus.ACTIVE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SavingsGoalDTO getById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));
    }

    @Transactional
    public SavingsGoalDTO create(SavingsGoalDTO dto) {
        SavingsGoal goal = toEntity(dto);
        return toDTO(repository.save(goal));
    }

    @Transactional
    public SavingsGoalDTO update(Long id, SavingsGoalDTO dto) {
        SavingsGoal goal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));

        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setCurrentAmount(dto.getCurrentAmount());
        goal.setTargetDate(dto.getTargetDate());
        goal.setMonthlyContribution(dto.getMonthlyContribution());
        goal.setPriority(dto.getPriority());
        goal.setStatus(dto.getStatus());
        goal.setIcon(dto.getIcon());
        goal.setColor(dto.getColor());

        // Check if goal is completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoal.GoalStatus.COMPLETED);
        }

        return toDTO(repository.save(goal));
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public SavingsGoalDTO addContribution(Long id, BigDecimal amount) {
        SavingsGoal goal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));

        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);

        // Check if goal is completed
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoal.GoalStatus.COMPLETED);
        }

        return toDTO(repository.save(goal));
    }

    @Transactional
    public SavingsGoalDTO withdrawAmount(Long id, BigDecimal amount) {
        SavingsGoal goal = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found: " + id));

        BigDecimal newAmount = goal.getCurrentAmount().subtract(amount);
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            newAmount = BigDecimal.ZERO;
        }
        goal.setCurrentAmount(newAmount);

        // If goal was completed and now is not, set back to active
        if (goal.getStatus() == SavingsGoal.GoalStatus.COMPLETED &&
                newAmount.compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus(SavingsGoal.GoalStatus.ACTIVE);
        }

        return toDTO(repository.save(goal));
    }

    private SavingsGoalDTO toDTO(SavingsGoal goal) {
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        Double percentage = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                    .doubleValue()
                : 0.0;

        Integer monthsToGoal = null;
        if (goal.getTargetDate() != null) {
            long months = ChronoUnit.MONTHS.between(LocalDate.now(), goal.getTargetDate());
            monthsToGoal = (int) Math.max(0, months);
        } else if (goal.getMonthlyContribution() != null &&
                   goal.getMonthlyContribution().compareTo(BigDecimal.ZERO) > 0 &&
                   remaining.compareTo(BigDecimal.ZERO) > 0) {
            monthsToGoal = remaining
                    .divide(goal.getMonthlyContribution(), 0, RoundingMode.CEILING)
                    .intValue();
        }

        return SavingsGoalDTO.builder()
                .id(goal.getId())
                .name(goal.getName())
                .description(goal.getDescription())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .targetDate(goal.getTargetDate())
                .monthlyContribution(goal.getMonthlyContribution())
                .priority(goal.getPriority())
                .status(goal.getStatus())
                .icon(goal.getIcon())
                .color(goal.getColor())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .remainingAmount(remaining)
                .percentageComplete(percentage)
                .monthsToGoal(monthsToGoal)
                .build();
    }

    private SavingsGoal toEntity(SavingsGoalDTO dto) {
        return SavingsGoal.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .targetAmount(dto.getTargetAmount())
                .currentAmount(dto.getCurrentAmount() != null ? dto.getCurrentAmount() : BigDecimal.ZERO)
                .targetDate(dto.getTargetDate())
                .monthlyContribution(dto.getMonthlyContribution())
                .priority(dto.getPriority() != null ? dto.getPriority() : SavingsGoal.Priority.MEDIUM)
                .status(dto.getStatus() != null ? dto.getStatus() : SavingsGoal.GoalStatus.ACTIVE)
                .icon(dto.getIcon())
                .color(dto.getColor())
                .build();
    }
}
