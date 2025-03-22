package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.ExpenseRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExpenseRepository expenseRepository;
    private final BudgetService budgetService;

    @Transactional
    public Expense save(Ticket ticket, int budgetId, double amountExpense) throws Exception {
        // update reste budget
        Budget budget = budgetService.findById(budgetId);
        budget.setAmountRemain(budget.getAmountRemain() - amountExpense);
        budgetService.save(budget);

        // insert expense
        Expense expense = new Expense();
        expense.setTicket(ticket);
        expense.setAmount(amountExpense);
        expense.setCreationDate(LocalDateTime.now());

        return expenseRepository.save(expense);
    }
}
