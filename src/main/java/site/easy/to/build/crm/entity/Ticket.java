package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import site.easy.to.build.crm.api.POV;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trigger_ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    @JsonView(POV.Expense.class)
    private int ticketId;

    @Column(name = "subject")
    @NotBlank(message = "Subject is required")
    @JsonView(POV.Expense.class)
    private String subject;

    @Column(name = "description")
    @JsonView(POV.Expense.class)
    private String description;

    @Column(name = "status")
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(open|assigned|on-hold|in-progress|resolved|closed|reopened|pending-customer-response|escalated|archived)$", message = "Invalid status")
    private String status;

    @Column(name = "priority")
    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "^(low|medium|high|closed|urgent|critical)$", message = "Invalid priority")
    private String priority;

    @JsonView(POV.Expense.class)
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @JsonView(POV.Expense.class)
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonView(POV.Expense.class)
    private Customer customer;

    @Column(name = "created_at")
    @JsonView(POV.Expense.class)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "budget_id")
    @JsonView(POV.Expense.class)
    private Budget budget;

    @JsonBackReference
    @OneToOne(mappedBy = "ticket")
    private Expense expense;

    public Ticket() {
    }

    public Ticket(String subject, String description, String status, String priority, User manager, User employee, Customer customer, LocalDateTime createdAt, Budget budget) {
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.manager = manager;
        this.employee = employee;
        this.customer = customer;
        this.createdAt = createdAt;
        this.budget = budget;
    }
}
