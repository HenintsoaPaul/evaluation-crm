package site.easy.to.build.crm.api.controller;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.easy.to.build.crm.api.POV;
import site.easy.to.build.crm.cpl.TicketCpl;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.service.ExpenseService;
import site.easy.to.build.crm.service.ticket.TicketServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketApiController {

    private final TicketServiceImpl ticketService;
    private final ExpenseService expenseService;

    @GetMapping
    @JsonView({POV.Expense.class})
    public List<Ticket> findAll() {
        return ticketService.findAll();
    }

    @GetMapping("/cpl")
    @JsonView({POV.Expense.class})
    public List<TicketCpl> findAllCpl() {
        return ticketService.findAllCpl();
    }

    @GetMapping("/{id}/expense")
    @JsonView({POV.Expense.class})
    public Expense findExpense(@PathVariable int id) {
        return expenseService.findByTicketId(id);
    }
}
