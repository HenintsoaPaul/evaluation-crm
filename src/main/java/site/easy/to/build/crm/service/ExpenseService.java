package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.HistoExpense;
import site.easy.to.build.crm.api.ApiServerException;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final DataDeleteService dataDeleteService;
    private final HistoExpenseService histoExpenseService;
    private final LeadRepository leadRepository;
    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;

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

    @Transactional
    public Expense update(Expense expense) {
        HistoExpense histoExpense = new HistoExpense();
        histoExpense.setAmount(expense.getAmount());
        histoExpense.setCreationDate(LocalDateTime.now());
        histoExpense.setExpense(expense);

        histoExpenseService.save(histoExpense);

        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    public List<Expense> findByCustomerId(int customerId) {
        return expenseRepository.findByCustomerId(customerId);
    }

    @Transactional
    public void deleteById(int expenseId) throws ApiServerException {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiServerException("Expense not found"));

        Lead lead = e.getLead();
        Ticket ticket = e.getTicket();
        if (lead == null) {
            ticketRepository.deleteById(ticket.getTicketId());
        } else if (ticket == null) {
            leadRepository.deleteById(lead.getLeadId());
        }

        dataDeleteService.deleteRowCascade("expense", expenseId + "");
    }

    @Transactional
    public HashMap<String, Object> updateById(int expenseId, double newAmount) throws ApiServerException {
        Expense expense = this.findById(expenseId);

        Budget budget;
        if (expense.getLead() == null) {
            budget = expense.getTicket().getBudget();
        } else {
            budget = expense.getLead().getBudget();
        }

        double oldAmount = expense.getAmount(),
                oldBudgetRemain = budget.getAmountRemain(),
                newBudgetRemain = oldBudgetRemain + oldAmount - newAmount;

        expense.setAmount(newAmount);
        budget.setAmountRemain(newBudgetRemain);

        HashMap<String, Object> map = new HashMap<>();
        map.put("expense", expense);
        map.put("budget", budget);

        budgetRepository.save(budget);
        this.update(expense);

        return map;
    }

    public Expense findById(int id) throws ApiServerException {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ApiServerException("Expense not found"));
    }

    public List<Expense> findAllTickets() {
        return expenseRepository.findAllTickets();
    }

    public List<Expense> findAllLeads() {
        return expenseRepository.findAllLeads();
    }

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }
}
