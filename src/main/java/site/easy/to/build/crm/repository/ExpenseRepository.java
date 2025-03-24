package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Expense;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    @Query(value = """
            SELECT e.*
            FROM expense e
            WHERE EXISTS (
                SELECT 1 FROM trigger_lead l
                WHERE l.lead_id = e.lead_id AND l.customer_id = :customerId
            ) OR EXISTS (
                SELECT 1 FROM trigger_ticket t
                WHERE t.ticket_id = e.ticket_id AND t.customer_id = :customerId
            )
            """, nativeQuery = true)
    List<Expense> findByCustomerId(int customerId);

    @Query(value = "SELECT e FROM Expense e WHERE e.ticket is not null")
    List<Expense> findAllTickets();

    @Query(value = "SELECT e FROM Expense e WHERE e.lead is not null")
    List<Expense> findAllLeads();
}
