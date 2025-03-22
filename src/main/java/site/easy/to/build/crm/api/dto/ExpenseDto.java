package site.easy.to.build.crm.api.dto;

import lombok.Data;

@Data
public class ExpenseDto {
    private final String expenseId;
    private final String ticketId;
    private final String leadId;
    private final double amount;
}
