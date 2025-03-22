package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.BudgetAlertConfig;
import site.easy.to.build.crm.service.BudgetAlertConfigService;

@Controller
@RequestMapping("/api/budget-alert-config")
@RequiredArgsConstructor
public class BudgetAlertConfigController {

    private final BudgetAlertConfigService budgetAlertConfigService;

    // crud methods
    // ...

    // api methods
    @GetMapping
    public ResponseEntity<BudgetAlertConfig> getCurrent() {
        return ResponseEntity.ok(budgetAlertConfigService.findCurrent());
    }

    @PostMapping
    public ResponseEntity<BudgetAlertConfig> create(@RequestParam double rate) {
        return ResponseEntity.ok(budgetAlertConfigService.save(rate));
    }
}
