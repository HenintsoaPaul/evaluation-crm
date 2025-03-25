package site.easy.to.build.crm.api.controller;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.api.POV;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.service.ExpenseService;
import site.easy.to.build.crm.service.lead.LeadServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadApiController {

    private final LeadServiceImpl leadService;
    private final ExpenseService expenseService;

    @GetMapping
    @JsonView({POV.Expense.class})
    public List<Lead> findAll() {
        return leadService.findAll();
    }

    @GetMapping("/{id}/expense")
    @JsonView({POV.Expense.class})
    public Expense findExpense(@PathVariable int id) {
        return expenseService.findByLeadId(id);
    }
}
