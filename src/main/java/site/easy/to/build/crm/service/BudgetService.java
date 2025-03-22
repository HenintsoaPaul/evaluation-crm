package site.easy.to.build.crm.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CustomerServiceImpl customerService;

    @Transactional
    public Budget save(@NotNull Budget budget, int customerId) throws Exception {
        budget.setCreationDate(LocalDateTime.now());
        budget.setAmountRemain(budget.getAmountLimit());

        Customer customer = customerService.findByCustomerId(customerId);
        if (customer == null) {
            throw new Exception("Client introuvable!");
        }
        budget.setCustomer(customer);

        return budgetRepository.save(budget);
    }
}
