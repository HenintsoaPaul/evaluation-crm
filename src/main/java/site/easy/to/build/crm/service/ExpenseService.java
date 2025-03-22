package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    private void decreaseBudgetRemaining(Budget budget, double amountExpense) {
        double remain = budget.getAmountRemain() - amountExpense;
        budget.setAmountRemain(remain);
        budgetRepository.save(budget);
    }

    @Transactional
    public Expense save(Ticket ticket, Budget budget, double amountExpense) throws Exception {
        decreaseBudgetRemaining(budget, amountExpense);

        Expense expense = new Expense();
        expense.setTicket(ticket);
        expense.setAmount(amountExpense);
        expense.setCreationDate(LocalDateTime.now());

        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense save(Lead lead, Budget budget, double amountExpense) throws Exception {
        decreaseBudgetRemaining(budget, amountExpense);

        Expense expense = new Expense();
        expense.setLead(lead);
        expense.setAmount(amountExpense);
        expense.setCreationDate(LocalDateTime.now());

        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    public List<Expense> findByCustomerId(int customerId) {
        return expenseRepository.findByCustomerId(customerId);
    }

    public void deleteById(int expenseId) {
        expenseRepository.deleteById(expenseId);
    }
}
