package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.csv.CsvValidationException;
import site.easy.to.build.crm.csv.GenericCsvService;
import site.easy.to.build.crm.csv.dto.CsvErrorWrapper;
import site.easy.to.build.crm.csv.dto.ExpenseCsvDto;
import site.easy.to.build.crm.entity.HistoExpense;
import site.easy.to.build.crm.api.ApiServerException;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.*;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final DataDeleteService dataDeleteService;
    private final HistoExpenseService histoExpenseService;
    private final LeadRepository leadRepository;
    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final GenericCsvService<ExpenseCsvDto, Expense> genericCsvService;
    private final BudgetTotalRepository budgetTotalRepository;
    private final JdbcTemplate jdbcTemplate;
    private final BudgetService budgetService;

    private void decreaseBudgetRemaining(int customerId, double amountExpense) {
        BudgetTotal budgetTotal = budgetTotalRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Budget total not found"));

        double remain = budgetTotal.getAmountRemain() - amountExpense;
        budgetTotal.setAmountRemain(remain);

        budgetTotalRepository.save(budgetTotal);
    }

    public List<String> getExpenseLog(BudgetAlertConfig bac, BudgetTotal budgetTotal, int expenseId) {
        double newBudgetRemain = budgetTotal.getAmountRemain(),
                budgetAmountTotal = budgetTotal.getAmountTotal(),
                alerte = budgetAmountTotal * (bac.getRate() / 100);

        List<String> messages = new ArrayList<>();
        messages.add("Modification du budget total '" + budgetTotal.getId() + "' par la mise a jour de l'expense '" + expenseId + "'");
        if (alerte <= newBudgetRemain) {
            messages.add("Seuil d'alerte de depense atteint pour le budget! seuil: " + alerte + " | reste: " + newBudgetRemain);
        }
        if (newBudgetRemain < 0) {
            messages.add("Depassement de budget! reste: " + newBudgetRemain);
        }
        return messages;
    }

    @Transactional
    public Expense save(Ticket ticket, double amountExpense) throws Exception {
        decreaseBudgetRemaining(ticket.getCustomer().getCustomerId(), amountExpense);

        Expense expense = new Expense();
        expense.setTicket(ticket);
        expense.setAmount(amountExpense);
        expense.setCreationDate(LocalDateTime.now());

        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense save(Lead lead, double amountExpense) throws Exception {
        decreaseBudgetRemaining(lead.getCustomer().getCustomerId(), amountExpense);

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

        int customerId;
        if (expense.getLead() == null) {
            customerId = expense.getTicket().getCustomer().getCustomerId();
        } else {
            customerId = expense.getLead().getCustomer().getCustomerId();
        }
        BudgetTotal budgetTotal = budgetTotalRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ApiServerException("Budget total not found"));

        double oldExpenseAmount = expense.getAmount(),
                oldBudgetRemain = budgetTotal.getAmountRemain(),
                newBudgetRemain = oldBudgetRemain + oldExpenseAmount - newAmount;

        expense.setAmount(newAmount);
        expenseRepository.save(expense);

        budgetTotal.setAmountRemain(newBudgetRemain);
        budgetTotalRepository.save(budgetTotal);


        HashMap<String, Object> map = new HashMap<>();
        map.put("expense", expense);
        map.put("budgetTotal", budgetTotal);
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

    public Expense findByLeadId(int id) {
        return expenseRepository.findByLeadId(id);
    }

    public Expense findByTicketId(int id) {
        return expenseRepository.findByTicketId(id);
    }

    // batch
    @Transactional
    public void saveBatch(List<Expense> expenses, Integer batchSize) {
        String sql = "INSERT INTO expense (creation_date, amount, lead_id, ticket_id) VALUES (?, ?, ?, ?)";
        Timestamp t = Timestamp.valueOf(LocalDateTime.now());

        int listSize = expenses.size();
        batchSize = (batchSize == null || batchSize > listSize) ? listSize : batchSize;

        jdbcTemplate.batchUpdate(sql, expenses, batchSize,
                (PreparedStatement ps, Expense expense) -> {
                    ps.setTimestamp(1, t);
                    ps.setDouble(2, expense.getAmount());

                    Integer leadId = (expense.getLead() != null) ? expense.getLead().getLeadId() : null;
                    Integer ticketId = (expense.getTicket() != null) ? expense.getTicket().getTicketId() : null;

                    ps.setObject(3, leadId, java.sql.Types.INTEGER);
                    ps.setObject(4, ticketId, java.sql.Types.INTEGER);
                });
    }

    // csv
    @Transactional
    public List<Expense> importCsv(MultipartFile file, User user) throws IOException, CsvValidationException {
        List<Expense> entities = new ArrayList<>();
        List<ExpenseCsvDto> dtos = new ArrayList<>();
        List<CsvErrorWrapper> errors = new ArrayList<>();

        String filename = file.getOriginalFilename();
        try {
            dtos = genericCsvService.getDtosFromCsv(file, ExpenseCsvDto.class, filename);
        } catch (CsvValidationException e) {
            errors.addAll(e.getErrors());
        }

        for (int i = 0; i < dtos.size(); i++) {
            entities.add(convertToEntity(dtos.get(i), user, errors, i + 1, filename));
        }

        if (!errors.isEmpty()) {
            throw new CsvValidationException("csv->entity", errors);
        }

        return entities;
    }

    @Transactional
    public Expense convertToEntity(
            ExpenseCsvDto csvDto,
            User user,
            List<CsvErrorWrapper> errors,
            int rowIndex,
            String filename
    ) {
        Expense expense = new Expense();
        expense.setAmount(csvDto.getExpense());

        double amountExpense = csvDto.getExpense();
        String email = csvDto.getCustomer_email();

        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            String msg = "Customer '" + email + "' not found!";
            errors.add(new CsvErrorWrapper(filename, rowIndex, msg, csvDto.toString()));
            return null;
        }

        try {
            decreaseBudget(customer.getCustomerId(), amountExpense, email);
        } catch (CsvValidationException ve) {
            errors.add(new CsvErrorWrapper(filename, rowIndex, ve.getMessage(), csvDto.toString()));
            return null;
        }

        if (csvDto.getType().equals("ticket")) {
            Ticket ticket = new Ticket();
            ticket.setCustomer(customer);
            ticket.setManager(user);
            ticket.setEmployee(null);
            ticket.setSubject(csvDto.getSubject_or_name());
            ticket.setStatus(csvDto.getStatus());
            ticket.setPriority("low");

            expense.setTicket(ticket);
            ticketRepository.save(ticket);

        } else if (csvDto.getType().equals("lead")) {
            Lead lead = new Lead();
            lead.setCustomer(customer);
            lead.setManager(user);
            lead.setEmployee(null);
            lead.setName(csvDto.getSubject_or_name());
            lead.setStatus(csvDto.getStatus());

            expense.setLead(lead);
            leadRepository.save(lead);

        } else {
            String msg = "Unknown expense type : '" + csvDto.getType() + "'! Please refer to existing type";
            errors.add(new CsvErrorWrapper(filename, rowIndex, msg, csvDto.toString()));
        }

        return expense;
    }

    private void decreaseBudget(int customerId, double amountExpense, String email) throws CsvValidationException {
        BudgetTotal budgetTotal = budgetTotalRepository.findByCustomerId(customerId).orElse(null);
        if (budgetTotal == null) {
            throw new CsvValidationException("No budget found for customer '" + email + "'!", null);
        }

        double amountRemain = budgetTotal.getAmountRemain();
        if (amountRemain < amountExpense) {
            String msg = "Budget of customer '" + customerId + "' exceeded expense amount! Remain: " + amountRemain + " | expense: " + amountExpense;
            throw new CsvValidationException(msg, null);
        }

        budgetTotal.setAmountRemain(amountRemain - amountExpense);
        budgetTotalRepository.save(budgetTotal);
    }
}
