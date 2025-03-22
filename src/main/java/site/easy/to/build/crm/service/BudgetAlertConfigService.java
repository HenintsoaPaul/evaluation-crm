package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.BudgetAlertConfig;
import site.easy.to.build.crm.repository.BudgetAlertConfigRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BudgetAlertConfigService {

    private final BudgetAlertConfigRepository budgetAlertConfigRepository;

    public BudgetAlertConfig findCurrent() {
        return budgetAlertConfigRepository.findCurrent();
    }

    @Transactional
    public BudgetAlertConfig save(double rate) {
        BudgetAlertConfig bac = new BudgetAlertConfig();
        bac.setRate(rate);
        bac.setCreationDate(LocalDateTime.now());
        return budgetAlertConfigRepository.save(bac);
    }
}
