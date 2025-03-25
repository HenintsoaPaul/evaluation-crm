package site.easy.to.build.crm.cpl;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import site.easy.to.build.crm.api.POV;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Ticket;

@Data
@AllArgsConstructor
public class TicketCpl {
    @JsonView(POV.Expense.class)
    private Ticket ticket;
    @JsonView(POV.Expense.class)
    private Expense expense;
}