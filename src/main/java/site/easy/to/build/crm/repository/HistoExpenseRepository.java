package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.HistoExpense;

import java.util.List;

@Repository
public interface HistoExpenseRepository extends JpaRepository<HistoExpense, Integer> {
}
