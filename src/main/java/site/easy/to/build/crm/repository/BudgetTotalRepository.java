package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.BudgetTotal;

import java.util.Optional;

@Repository
public interface BudgetTotalRepository extends JpaRepository<BudgetTotal, Integer> {

    @Query("select b from BudgetTotal b where b.customer.customerId = :customerId")
    Optional<BudgetTotal> findByCustomerId(int customerId);
}
