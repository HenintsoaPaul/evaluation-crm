package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import site.easy.to.build.crm.api.POV;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "expense")
public class Expense {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({POV.Expense.class, POV.Dashboard.class})
    private Integer id;

    @NotNull(message = "Amount cannot be null")
    @Min(value = 0, message = "Min amount is 0")
    @Column(name = "amount", nullable = false)
    @JsonView({POV.Expense.class, POV.Dashboard.class})
    private Double amount;

    @NotNull
    @Column(name = "creation_date", nullable = false)
    @JsonView({POV.Expense.class, POV.Dashboard.class})
    private LocalDateTime creationDate;

    @OneToOne
    @JoinColumn(name = "lead_id", unique = true)
    @JsonView(POV.Expense.class)
    private Lead lead;

    @OneToOne
    @JoinColumn(name = "ticket_id", unique = true)
    @JsonView(POV.Expense.class)
    private Ticket ticket;

    // Validation personnalisée pour garantir lead OU ticket
    @AssertTrue
    private boolean isValid() {
        return (lead != null) ^ (ticket != null);
    }

}