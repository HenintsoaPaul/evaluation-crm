package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import site.easy.to.build.crm.api.POV;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "budget")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView({POV.Expense.class, POV.Budget.class})
    private Integer id;

    @Column(name = "creation_date")
    @JsonView({POV.Expense.class, POV.Budget.class})
    private LocalDateTime creationDate;

    @NotNull
    @Column(name = "amount", nullable = false)
    @JsonView({POV.Expense.class, POV.Budget.class})
    private Double amount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;
}
