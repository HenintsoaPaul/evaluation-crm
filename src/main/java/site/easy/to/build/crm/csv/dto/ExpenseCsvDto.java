package site.easy.to.build.crm.csv.dto;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ExpenseCsvDto {
    @NotBlank(message = "'customer_email' cannot be blank")
    @Email(message = "Please enter a valid email format for 'customer_email'")
    @Size(max = 255, message = "'customer_email' must be less than 255 characters")
    @CsvBindByName(column = "customer_email")
    private String customer_email;

    @NotBlank(message = "'subject_or_name' cannot be blank")
    @CsvBindByName(column = "subject_or_name")
    private String subject_or_name;

    @NotBlank(message = "'status' cannot be blank")
    @Pattern(regexp = "^(lead|ticket)$", message = "Invalid type")
    @CsvBindByName(column = "type")
    private String type;

    @NotBlank(message = "'status' cannot be blank")
    @Pattern(regexp = "^(open|assigned|on-hold|in-progress|resolved|closed|reopened|pending-customer-response|escalated|archived|meeting-to-schedule|assign-to-sales|success)$", message = "Invalid status")
    @CsvBindByName(column = "status")
    private String status;

    @NotBlank(message = "'expenseStr' cannot be blank")
    @CsvBindByName(column = "expense")
    private String expenseStr;

    @DecimalMin(value = "0.0", inclusive = false, message = "'expense' must be strictly sup to 0.0")
    public double expense;

    // Custom setter for expense to handle comma replacement
    public void setExpenseStr(String expense) {
        if (expense.contains(",")) {
            this.expense = Double.parseDouble(expense.replace(",", "."));
        } else {
            this.expense = Double.parseDouble(expense);
        }
        this.expenseStr = expense;
    }
}
