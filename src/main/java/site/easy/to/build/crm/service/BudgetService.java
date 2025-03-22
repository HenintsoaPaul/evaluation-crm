package site.easy.to.build.crm.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.repository.BudgetRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    @Transactional
    public Budget save(@NotNull Budget budget) throws Exception {
        budget.setCreationDate(LocalDateTime.now());
        budget.setAmountRemain(budget.getAmountLimit());

        return budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    public Budget findById(int id) {
        return budgetRepository.findById(id).orElse(null);
    }
}
