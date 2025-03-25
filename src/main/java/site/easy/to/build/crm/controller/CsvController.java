package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.csv.CsvValidationException;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.service.BudgetService;
import site.easy.to.build.crm.service.ExpenseService;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.util.List;

@Controller
@RequestMapping("/import/csv")
@RequiredArgsConstructor
public class CsvController {

    private final ExpenseService expenseService;
    private final UserServiceImpl userService;
    private final CustomerServiceImpl customerService;
    private final AuthenticationUtils authenticationUtils;
    private final BudgetService budgetService;

    @PostMapping("/user")
    public String csvEspace(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            int userId = authenticationUtils.getLoggedInUserId(authentication);
            User loggedInUser = userService.findById(userId);
            if (loggedInUser.isInactiveUser()) {
                return "error/account-inactive";
            }
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);

            List<User> users = userService.importCsv(file, oAuthUser);

            String msg = "Fichier CSV traité avec succès : " + users.size() + " lignes insérées";
            redirectAttributes.addFlashAttribute("message", msg);
        } catch (CsvValidationException e) {
            redirectAttributes.addFlashAttribute("validationErrors", e.getErrors());
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/manager/register-user";
    }

    @PostMapping("/customer")
    public String fichier3(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false, defaultValue = "false") boolean sendEmail
    ) {
        try {
            int userId = authenticationUtils.getLoggedInUserId(authentication);
            User loggedInUser = userService.findById(userId);
            if (loggedInUser.isInactiveUser()) {
                return "error/account-inactive";
            }

            // import csv
            List<Customer> customers = customerService.importCsv(file, loggedInUser, authentication, sendEmail);

            String msg = "Fichier CSV traité avec succès : " + customers.size() + " lignes insérées";
            redirectAttributes.addFlashAttribute("message", msg);
        } catch (CsvValidationException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorImp", e.getErrors());
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("errorImp", ex.getMessage());
        }
        return "redirect:/data/management/izy";
    }

    @PostMapping("/expense")
    public String fichier1(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            int userId = authenticationUtils.getLoggedInUserId(authentication);
            User loggedInUser = userService.findById(userId);
            if (loggedInUser.isInactiveUser()) {
                return "error/account-inactive";
            }

            // import csv
            List<Expense> expenses = expenseService.importCsv(file, loggedInUser);

            String msg = "Fichier CSV traité avec succès : " + expenses.size() + " lignes insérées";
            redirectAttributes.addFlashAttribute("messageImp", msg);
        } catch (CsvValidationException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorImp", e.getErrors());
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("errorImp", ex.getMessage());
        }
        return "redirect:/data/management/izy";
    }

    @PostMapping("/budget")
    public String budget(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            int userId = authenticationUtils.getLoggedInUserId(authentication);
            User loggedInUser = userService.findById(userId);
            if (loggedInUser.isInactiveUser()) {
                return "error/account-inactive";
            }

            // import csv
            List<Budget> expenses = budgetService.importCsv(file);

            String msg = "Fichier CSV traité avec succès : " + expenses.size() + " lignes insérées";
            redirectAttributes.addFlashAttribute("messageImp", msg);
        } catch (CsvValidationException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorImp", e.getErrors());
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("errorImp", ex.getMessage());
        }
        return "redirect:/data/management/izy";
    }
}
