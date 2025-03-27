package site.easy.to.build.crm.cpl;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import site.easy.to.build.crm.api.POV;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.BudgetTotal;
import site.easy.to.build.crm.entity.Customer;

import java.util.List;

@Data
public class BudgetCpl {
    @JsonView(POV.Budget.class)
    private final Customer customer;
    @JsonView(POV.Budget.class)
    private final BudgetTotal budgetTotal;
    @JsonView(POV.Budget.class)
    private final List<Budget> budgets;
}
