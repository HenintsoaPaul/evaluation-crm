package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.BudgetAlertConfig;

@Repository
public interface BudgetAlertConfigRepository extends JpaRepository<BudgetAlertConfig, Integer> {
    @Query("select b from BudgetAlertConfig b order by b.id limit 1")
    BudgetAlertConfig findCurrent();
}
