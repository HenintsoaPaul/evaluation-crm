package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    @Transactional
    public Expense save(Ticket ticket, Budget budget, double amountExpense) throws Exception {
        // update reste budget
        double remain = budget.getAmountRemain() - amountExpense;
        budget.setAmountRemain(remain);
        budgetRepository.save(budget);

        // insert expense
        Expense expense = new Expense();
        expense.setTicket(ticket);
        expense.setAmount(amountExpense);
        expense.setCreationDate(LocalDateTime.now());

        return expenseRepository.save(expense);
    }
}
