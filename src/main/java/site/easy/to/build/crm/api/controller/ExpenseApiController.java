package site.easy.to.build.crm.api.controller;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.api.*;
import site.easy.to.build.crm.entity.BudgetAlertConfig;
import site.easy.to.build.crm.entity.BudgetTotal;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.repository.BudgetTotalRepository;
import site.easy.to.build.crm.service.BudgetAlertConfigService;
import site.easy.to.build.crm.service.ExpenseService;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseApiController {

    private final BudgetTotalRepository budgetTotalRepository;

    @AllArgsConstructor
    private class ExpenseCpl {
        @JsonView(POV.Expense.class)
        private Expense expense;
        @JsonView(POV.Expense.class)
        private BudgetTotal budgetTotal;
    }

    private final ExpenseService expenseService;
    private final BudgetAlertConfigService budgetAlertConfigService;

    @GetMapping("/dashboard")
    @JsonView({POV.Dashboard.class})
    public HashMap<String, List<?>> findDashboardData() {
        HashMap<String, List<?>> maps = new HashMap<>();
        maps.put("all", expenseService.findAll());
        maps.put("lead", expenseService.findAllLeads());
        maps.put("ticket", expenseService.findAllTickets());
        maps.put("customer", expenseService.findAllCustomers());

        return maps;
    }

    @GetMapping
    @JsonView({POV.Expense.class})
    public List<Expense> findAll() {
        return expenseService.findAll();
    }

    @GetMapping("/tickets")
    @JsonView({POV.Expense.class})
    public List<Expense> findAllTickets() {
        return expenseService.findAllTickets();
    }

    @GetMapping("/leads")
    @JsonView({POV.Expense.class})
    public List<Expense> findAllLeads() {
        return expenseService.findAllLeads();
    }

    @GetMapping("/{id}")
    @JsonView({POV.Expense.class})
    public ExpenseCpl findById(@PathVariable int id) throws ApiServerException {
        Expense e = expenseService.findById(id);

        int customerId;
        if (e.getLead() != null) {
            customerId = e.getLead().getCustomer().getCustomerId();
        } else {
            customerId = e.getTicket().getCustomer().getCustomerId();
        }
        BudgetTotal bt = budgetTotalRepository.findByCustomerId(customerId).orElse(null);

        return new ExpenseCpl(e, bt);
    }

    @GetMapping("/by-client")
    @JsonView({POV.Expense.class})
    public List<Expense> findByClient(@RequestParam int clientId) {
        return expenseService.findByCustomerId(clientId);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<?>> update(
            @PathVariable(name = "id") int expenseId,
            @RequestParam double newAmount
    ) throws ApiServerException {
        ApiResponse<?> response;
        try {
            HashMap<String, Object> map = expenseService.updateById(expenseId, newAmount);

            BudgetTotal budgetTotal = (BudgetTotal) map.get("budgetTotal");
            BudgetAlertConfig bac = budgetAlertConfigService.findCurrent();

            response = new ApiOkResponse<>("Data mis a jour", expenseService.getExpenseLog(bac, budgetTotal, expenseId));
            return ResponseEntity.ok(response);

        } catch (ApiServerException e) {
            response = new ApiBadResponse<>(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable(name = "id") int expenseId) {
        ApiResponse<?> response;
        try {
            expenseService.deleteById(expenseId);
            response = new ApiOkResponse<>("Data efface", expenseId);
            return ResponseEntity.ok(response);
        } catch (ApiServerException e) {
            response = new ApiBadResponse<>(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
