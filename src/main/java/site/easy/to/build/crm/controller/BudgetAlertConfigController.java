package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.api.ApiBadResponse;
import site.easy.to.build.crm.api.ApiOkResponse;
import site.easy.to.build.crm.api.ApiResponse;
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
    public ResponseEntity<ApiResponse<?>> create(@RequestParam double rate) {
        ApiResponse<?> response;

        boolean isValid = rate >= 0 && rate <= 100;
        if (!isValid) {
            response = new ApiBadResponse<>("Valeur hors borne! Rate must be between 0 and 100");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        BudgetAlertConfig bac = budgetAlertConfigService.save(rate);

        response = new ApiOkResponse<>("Insertion ok", bac);
        return ResponseEntity.ok(response);
    }
}
