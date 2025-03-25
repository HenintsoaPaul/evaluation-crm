package site.easy.to.build.crm.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.csv.CsvValidationException;
import site.easy.to.build.crm.csv.GenericCsvService;
import site.easy.to.build.crm.csv.dto.BudgetCsvDto;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.BudgetTotalRepository;
import site.easy.to.build.crm.repository.CustomerRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetTotalRepository budgetTotalRepository;
    private final GenericCsvService<BudgetCsvDto, Budget> genericCsvService;
    private final CustomerRepository customerRepository;

    @Transactional
    public Budget save(@NotNull Budget budget) throws Exception {
        BudgetTotal bt = budgetTotalRepository.findByCustomerId(budget.getCustomer().getCustomerId())
                .orElse(null);

        if (bt == null) {
            bt = new BudgetTotal();
            bt.setCustomer(budget.getCustomer());
            bt.setAmountTotal(budget.getAmount());
            bt.setAmountRemain(budget.getAmount());
        } else {
            double oldRemain = bt.getAmountRemain(),
                    oldTotal = bt.getAmountTotal();
            bt.setAmountTotal(oldTotal + budget.getAmount());
            bt.setAmountRemain(oldRemain + budget.getAmount());
        }
        budgetTotalRepository.save(bt);

        budget.setCreationDate(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    public Budget findById(int id) {
        return budgetRepository.findById(id).orElse(null);
    }

    public List<Budget> findByCustomerId(int customerId) {
        return budgetRepository.findByCustomerId(customerId);
    }

    // csv
    @Transactional
    public List<Budget> importCsv(MultipartFile file) throws IOException, CsvValidationException {
        List<Budget> entities = new ArrayList<>();
        for (BudgetCsvDto dto : genericCsvService.getDtosFromCsv(file, BudgetCsvDto.class)) {
            entities.add(convertToEntity(dto));
        }
        this.budgetRepository.saveAll(entities);
        return entities;
    }

    @Transactional
    public Budget convertToEntity(
            BudgetCsvDto csvDto)
            throws CsvValidationException {
        Budget budget = new Budget();
        double amount = csvDto.getBudget();

        Customer customer = customerRepository.findByEmail(csvDto.getCustomer_email());
        if (customer == null) {
            throw new CsvValidationException("Customer '" + csvDto.getCustomer_email() + "' not found!", null);
        }

        BudgetTotal bt = budgetTotalRepository.findByCustomerId(customer.getCustomerId()).orElse(null);
        if (bt == null) {
            bt = new BudgetTotal();
            bt.setCustomer(customer);
            bt.setAmountTotal(amount);
            bt.setAmountRemain(amount);
        } else {
            double oldRemain = bt.getAmountRemain(),
                    oldTotal = bt.getAmountTotal();
            bt.setAmountTotal(oldTotal + amount);
            bt.setAmountRemain(oldRemain + amount);
        }

        budgetTotalRepository.save(bt);

        // budget
        budget.setAmount(csvDto.getBudget());
        budget.setCustomer(customer);
        budget.setCreationDate(LocalDateTime.now());

        return budget;
    }
}
