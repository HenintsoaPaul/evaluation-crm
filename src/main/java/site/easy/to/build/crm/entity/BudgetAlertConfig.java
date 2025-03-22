package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "budget_alert_config")
public class BudgetAlertConfig {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "rate", nullable = false)
    private Double rate;

    @NotNull
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

}