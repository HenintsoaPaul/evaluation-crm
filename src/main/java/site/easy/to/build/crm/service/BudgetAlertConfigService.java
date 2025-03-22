package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.BudgetAlertConfig;
import site.easy.to.build.crm.repository.BudgetAlertConfigRepository;

@Service
@RequiredArgsConstructor
public class BudgetAlertConfigService {

    private final BudgetAlertConfigRepository budgetAlertConfigRepository;

    public BudgetAlertConfig findCurrent() {
        return budgetAlertConfigRepository.findCurrent();
    }
}
