package site.easy.to.build.crm.api.controller;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.api.*;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.BudgetAlertConfig;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.service.BudgetAlertConfigService;
import site.easy.to.build.crm.service.ExpenseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/expense")
@RequiredArgsConstructor
public class ExpenseApiController {

    private final ExpenseService expenseService;
    private final BudgetAlertConfigService budgetAlertConfigService;

//    @GetMapping
//    public ResponseEntity<ApiResponse<?>> findAll() {
//        List<Expense> expenses = expenseService.findAll();
//
//        MappingJacksonValue body = new MappingJacksonValue(expenses);
//        body.setSerializationView(POV.Expense.class);
//
//        ApiResponse<?> response = new ApiOkResponse<>("Toutes les depenses.", body);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping
    @JsonView({POV.Expense.class})
    public List<Expense> findAll() {
        return expenseService.findAll();
    }

//    @GetMapping("/by-client")
//    public ResponseEntity<ApiResponse<?>> findByClient(@RequestParam int clientId) {
//        List<Expense> expenses = expenseService.findByCustomerId(clientId);
//        ApiResponse<?> response = new ApiOkResponse<>("Toutes les depenses pour le customer " + clientId, expenses);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/by-client")
    @JsonView({POV.Expense.class})
    public List<Expense> findByClient(@RequestParam int clientId) {
        return expenseService.findByCustomerId(clientId);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> update(@RequestParam int expenseId, @RequestParam double newAmount) {
        ApiResponse<?> response;
        try {
            HashMap<String, Object> map = expenseService.updateById(expenseId, newAmount);
            Budget budget = (Budget) map.get("budget");
            double newBudgetRemain = budget.getAmountRemain();

            BudgetAlertConfig bac = budgetAlertConfigService.findCurrent();
            double alerte = budget.getAmountLimit() * (bac.getRate() / 100);

            List<String> messages = new ArrayList<>();
            messages.add("Modification du budget '" + budget.getName() + "' par la mise a jour de l'expense '" + expenseId + "'");
            if (alerte <= newBudgetRemain) {
                messages.add("Seuil d'alerte de depense atteint pour le budget! seuil: " + alerte + " | reste: " + newBudgetRemain);
            }
            if (newBudgetRemain < 0) {
                messages.add("Depassement de budget! reste: " + newBudgetRemain);
            }

            response = new ApiOkResponse<>("Data mis a jour", messages);
            return ResponseEntity.ok(response);
        } catch (ApiServerException e) {
            response = new ApiBadResponse<>(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> delete(@RequestParam int expenseId) {
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
