package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final AuthenticationUtils authenticationUtils;
    private final UserServiceImpl userService;
    private final CustomerServiceImpl customerService;

    // crud methods
    @GetMapping
    public String showList(
            Model model,
            Authentication authentication
    ) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        model.addAttribute("budgets", budgetService.findAll());
        return "budget/all-budgets";
    }

    @GetMapping("/update-budget/{id}")
    public String showFormUpdate(
            Model model,
            Authentication authentication,
            @PathVariable int id
    ) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if (user.isInactiveUser()) {
            return "error/account-inactive";
        }

        Budget budget = budgetService.findById(id);
        if (budget == null) {
            return "error/not-found";
        }

        populateModelAttributes(model, authentication, user);

        model.addAttribute("budget", budget);
        return "budget/update-budget";
    }

    @GetMapping("/create-budget")
    public String showFormCreate(
            Model model,
            Authentication authentication
    ) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if (user.isInactiveUser()) {
            return "error/account-inactive";
        }

        populateModelAttributes(model, authentication, user);

        model.addAttribute("budget", new Budget());
        return "budget/create-budget";
    }

    @PostMapping("/save-budget")
    public String processFormd(
            @ModelAttribute("budget") Budget budget,
            RedirectAttributes redirectAttributes
    ) {
        try {
            budgetService.save(budget);
            redirectAttributes.addFlashAttribute("message", "Budget created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/budget";
    }

    // api methods
    @GetMapping("/api")
    public ResponseEntity<List<Budget>> getBudgetsByCustomer(@RequestParam int customerId) {
        List<Budget> budgets = budgetService.findByCustomerId(customerId);
        return ResponseEntity.ok(budgets);
    }

    //    methods
    private void populateModelAttributes(Model model, Authentication authentication, User loggedInUser) {
        List<User> employees = new ArrayList<>();
        List<Customer> customers;

        if (AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            employees = userService.findAll();
            customers = customerService.findAll();
        } else {
            employees.add(loggedInUser);
            customers = customerService.findByUserId(loggedInUser.getId());
        }

        model.addAttribute("employees", employees);
        model.addAttribute("customers", customers);
    }
}
