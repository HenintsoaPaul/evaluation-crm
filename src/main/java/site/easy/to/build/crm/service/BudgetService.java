package site.easy.to.build.crm.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.BudgetTotal;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.BudgetTotalRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetTotalRepository budgetTotalRepository;

    @Transactional
    public Budget save(@NotNull Budget budget) throws Exception {
        BudgetTotal bt = budgetTotalRepository.findByCustomerId(budget.getCustomer().getCustomerId())
                .orElse(null);

        if (bt == null) {
            bt = new BudgetTotal();
            bt.setCustomer(budget.getCustomer());
            bt.setAmountTotal(budget.getAmount());
            bt.setAmountRemain(budget.getAmount());
        } else {
            double oldRemain = bt.getAmountRemain(),
                    oldTotal = bt.getAmountTotal();
            bt.setAmountTotal(oldTotal + budget.getAmount());
            bt.setAmountRemain(oldRemain + budget.getAmount());
        }
        budgetTotalRepository.save(bt);

        budget.setCreationDate(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

//    public Budget updateBudget(Budget budget) {
//        BudgetTotal bt = budgetTotalRepository.findByCustomerId(budget.getCustomer().getCustomerId())
//                .orElseThrow(() -> new RuntimeException("Budget total not found"));
//
//        assert bt != null;
//        double oldRemain = bt.getAmountRemain(),
//                oldTotal = bt.getAmountTotal(),
//        newRemain = oldRemain ;
//
//        bt.setAmountTotal(oldTotal + budget.getAmount());
//    }

    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    public Budget findById(int id) {
        return budgetRepository.findById(id).orElse(null);
    }

    public List<Budget> findByCustomerId(int customerId) {
        return budgetRepository.findByCustomerId(customerId);
    }
}
