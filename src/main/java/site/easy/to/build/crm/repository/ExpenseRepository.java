package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Expense;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    @Query("select e from Expense e where e.lead.customer.customerId = :customerId or e.ticket.customer.customerId = :customerId")
    List<Expense> findByCustomerId(int customerId);
}
