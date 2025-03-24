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
    @JsonView(POV.Expense.class)
    private Integer id;

    @Column(name = "name")
    @JsonView(POV.Expense.class)
    private String name;

    @Column(name = "creation_date")
    @JsonView(POV.Expense.class)
    private LocalDateTime creationDate;

    @NotNull
    @Column(name = "amount_limit", nullable = false)
    @JsonView(POV.Expense.class)
    private Double amountLimit;

    @NotNull
    @Column(name = "amount_remain", nullable = false)
    @JsonView(POV.Expense.class)
    private Double amountRemain;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;
}
