package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.ExpenseService;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;

@Controller
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final AuthenticationUtils authenticationUtils;
    private final UserServiceImpl userService;

    // crud methods
    @GetMapping
    public String showList(
            Model model,
            Authentication authentication
    ) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if (user.isInactiveUser()) {
            return "error/account-inactive";
        }

        model.addAttribute("expenses", expenseService.findAll());
        return "expense/all-expenses";
    }

    // api methods
//    @GetMapping("/api")
//    public ResponseEntity<List<Budget>> getBudgetsByCustomer(@RequestParam int customerId) {
//        List<Budget> expenses = expenseService.findByCustomerId(customerId);
//        return ResponseEntity.ok(expenses);
//    }
}
