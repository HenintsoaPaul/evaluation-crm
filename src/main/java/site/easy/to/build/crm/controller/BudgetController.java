package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.BudgetTotal;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.repository.BudgetTotalRepository;
import site.easy.to.build.crm.service.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

import java.util.List;

@Controller
@RequestMapping("/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final AuthenticationUtils authenticationUtils;
    private final UserServiceImpl userService;
    private final CustomerServiceImpl customerService;
    private final BudgetTotalRepository budgetTotalRepository;

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

        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        populateModelAttributes(model);
        model.addAttribute("budget", new Budget());
        return "budget/create-budget";
    }

    @PostMapping("/create-budget")
    public String processFormd(
            @ModelAttribute("budget") @Validated Budget budget,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Authentication authentication,
            Model model
    ) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User manager = userService.findById(userId);
        if (manager == null || budget == null) {
            return "error/500";
        } else if (manager.isInactiveUser()) {
            return "error/account-inactive";
        }

        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        if (bindingResult.hasErrors()) {
            populateModelAttributes(model);
            return "budget/create-budget";
        }

        try {
            budgetService.save(budget);
            redirectAttributes.addFlashAttribute("message", "Budget created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/budget";
    }

    //    @GetMapping("/update-budget/{id}")
//    public String showFormUpdate(
//            Model model,
//            Authentication authentication,
//            @PathVariable int id
//    ) {
//        int userId = authenticationUtils.getLoggedInUserId(authentication);
//        User user = userService.findById(userId);
//        if (user.isInactiveUser()) {
//            return "error/account-inactive";
//        }
//
//        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
//            return "error/access-denied";
//        }
//
//        Budget budget = budgetService.findById(id);
//        if (budget == null) {
//            return "error/not-found";
//        }
//
//        populateModelAttributes(model);
//        model.addAttribute("budget", budget);
//        return "budget/update-budget";
//    }

//    @PostMapping("/update-budget")
//    public String processFormUpdate(
//            @ModelAttribute("budget") @Validated Budget budget,
//            BindingResult bindingResult,
//            RedirectAttributes redirectAttributes,
//            Authentication authentication,
//            Model model
//    ) {
//        int userId = authenticationUtils.getLoggedInUserId(authentication);
//        User manager = userService.findById(userId);
//        if (manager == null || budget == null) {
//            return "error/500";
//        } else if (manager.isInactiveUser()) {
//            return "error/account-inactive";
//        }
//
//        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
//            return "error/access-denied";
//        }
//
//        if (bindingResult.hasErrors()) {
//            populateModelAttributes(model);
//            return "budget/update-budget";
//        }
//
//        try {
//            budgetService.save(budget);
//            redirectAttributes.addFlashAttribute("message", "Budget updated successfully");
//        } catch (Exception e) {
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/budget";
//    }

    // api methods
    @GetMapping("/api")
    public ResponseEntity<List<Budget>> getBudgetsByCustomer(@RequestParam int customerId) {
        List<Budget> budgets = budgetService.findByCustomerId(customerId);
        return ResponseEntity.ok(budgets);
    }

    // api methods
    @GetMapping("/api/total")
    public ResponseEntity<BudgetTotal> getBudgetTotalByCustomer(@RequestParam int customerId) {
        BudgetTotal budgetTotal = budgetTotalRepository.findByCustomerId(customerId).orElse(null);
        return ResponseEntity.ok(budgetTotal);
    }

    //    methods
    private void populateModelAttributes(Model model) {
        model.addAttribute("employees", userService.findAll());
        model.addAttribute("customers", customerService.findAll());
    }
}
