package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.BudgetTotalRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;
import site.easy.to.build.crm.service.BudgetAlertConfigService;
import site.easy.to.build.crm.service.ExpenseService;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

@Controller
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final AuthenticationUtils authenticationUtils;
    private final UserServiceImpl userService;
    private final ExpenseRepository expenseRepository;
    private final BudgetAlertConfigService budgetAlertConfigService;
    private final BudgetTotalRepository budgetTotalRepository;

    // crud methods
    @GetMapping
    public String showList(
            Model model,
            Authentication authentication
    ) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedUser = userService.findById(userId);
        if (loggedUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        model.addAttribute("expenses", expenseService.findAll());
        return "expense/all-expenses";
    }

    @GetMapping("/update-expense/{id}")
    public String showFormUpdate(
            Model model,
            Authentication authentication,
            @PathVariable int id
    ) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedUser = userService.findById(userId);
        if (loggedUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense == null) {
            return "error/not-found";
        }

        int customerId;
        if (expense.getLead() == null) {
            customerId = expense.getTicket().getCustomer().getCustomerId();
        } else {
            customerId = expense.getLead().getCustomer().getCustomerId();
        }
        BudgetTotal budgetTotal = budgetTotalRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Budget Total not found"));

        model.addAttribute("budgetAlertConfig", budgetAlertConfigService.findCurrent());
        model.addAttribute("budgetTotal", budgetTotal);
        model.addAttribute("expense", expense);
        return "expense/update-expense";
    }

    @PostMapping("/update-expense")
    public String processFormUpdate(
            @ModelAttribute("expense") Expense expense,
            RedirectAttributes redirectAttributes,
            Authentication authentication
    ) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User manager = userService.findById(userId);
        if (manager == null || expense == null) {
            return "error/500";
        } else if (manager.isInactiveUser()) {
            return "error/account-inactive";
        }

        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        try {
            expenseService.updateById(expense.getId(), expense.getAmount());
            redirectAttributes.addFlashAttribute("message", "Expense updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/expense";
    }
}
