package site.easy.to.build.crm.api.controller;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.api.*;
import site.easy.to.build.crm.cpl.BudgetCpl;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.BudgetTotal;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.BudgetTotalRepository;
import site.easy.to.build.crm.repository.CustomerRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetApiController {

    private final CustomerRepository customerRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetTotalRepository budgetTotalRepository;

    @GetMapping
    @JsonView(POV.Budget.class)
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    @GetMapping("/{id}")
    @JsonView(POV.Budget.class)
    public Budget findById(@PathVariable int id) {
        return budgetRepository.findById(id).orElse(null);
    }

    @GetMapping("/cpl")
    @JsonView(POV.Budget.class)
    public List<BudgetCpl> findAllCpl() {
        List<BudgetCpl> r = new ArrayList<>();

        for (BudgetTotal budgetTotal : budgetTotalRepository.findAll()) {
            int customerId = budgetTotal.getCustomer().getCustomerId();
            Customer c = customerRepository.findByCustomerId(customerId);
            List<Budget> budgets = budgetRepository.findByCustomerId(customerId);
            r.add(new BudgetCpl(c, budgetTotal, budgets));
        }

        return r;
    }

    @GetMapping("/cpl/{customerId}")
    @JsonView(POV.Budget.class)
    public BudgetCpl findByCustomerId(@PathVariable int customerId) {
        BudgetTotal budgetTotal = budgetTotalRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("No budget found for customer id " + customerId));
        Customer c = customerRepository.findByCustomerId(customerId);
        return new BudgetCpl(c, budgetTotal, budgetRepository.findByCustomerId(customerId));
    }
}
