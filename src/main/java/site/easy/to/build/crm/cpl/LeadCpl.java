package site.easy.to.build.crm.cpl;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import site.easy.to.build.crm.api.POV;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;

@Data
@AllArgsConstructor
public class LeadCpl {
    @JsonView(POV.Expense.class)
    private Lead lead;
    @JsonView(POV.Expense.class)
    private Expense expense;
}