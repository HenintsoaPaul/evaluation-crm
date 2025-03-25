package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.csv.CsvValidationException;
import site.easy.to.build.crm.csv.dto.CsvErrorWrapper;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.service.BudgetService;
import site.easy.to.build.crm.service.ExpenseService;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.util.ArrayList;
import java.util.HashMap;
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
    private final PlatformTransactionManager transactionManager;

    @GetMapping("/all")
    public String showall(Model model) {
        return "/data-management/csv";
    }

    @PostMapping("/all")
    public String all(
            @RequestParam("fileCustomer") MultipartFile fileCustomer,
            @RequestParam("fileBudget") MultipartFile fileBudget,
            @RequestParam("fileExpense") MultipartFile fileExpense,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false, defaultValue = "false") boolean controlSumExpenseVsSumBudget,
            Model model
    ) {
        try {
            int userId = authenticationUtils.getLoggedInUserId(authentication);
            User loggedInUser = userService.findById(userId);
            if (loggedInUser.isInactiveUser()) {
                return "error/account-inactive";
            }

            HashMap<String, List<CsvErrorWrapper>> errorsPerFile = new HashMap<>();
            List<Customer> customers = new ArrayList<>();
            List<Budget> budgets = new ArrayList<>();
            List<Expense> expenses = new ArrayList<>();

            // import csv
            try {
                customers = customerService.importCsv(fileCustomer, loggedInUser);
            } catch (CsvValidationException e) {
                errorsPerFile.put("customers", e.getErrors());
            }
            try {
                budgets = budgetService.importCsv(fileBudget);
            } catch (CsvValidationException e) {
                errorsPerFile.put("budgets", e.getErrors());
            }
            try {
                expenses = expenseService.importCsv(fileExpense, loggedInUser);
            } catch (CsvValidationException e) {
                errorsPerFile.put("expenses", e.getErrors());
            }

            // control expense > budget
            if (controlSumExpenseVsSumBudget) {
                validateSumExpenseVsSumBudget(expenses, budgets, errorsPerFile);
            }

            // batch save
            this.saveBatches(customers, budgets, expenses);

            if (errorsPerFile.isEmpty()) {
                setSuccessAttributes(redirectAttributes, customers, budgets, expenses);
            } else {
                setErrorAttributes(model, errorsPerFile);
                return "data-management/csv-errors";
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("errorImp", ex.getMessage());
        }
        return "redirect:/import/csv/all";
    }

    // methods
    private void validateSumExpenseVsSumBudget(List<Expense> expenses, List<Budget> budgets,
                                               HashMap<String, List<CsvErrorWrapper>> errorsPerFile) {
        double sumExpense = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double sumBudget = budgets.stream().mapToDouble(Budget::getAmount).sum();

        if (sumExpense > sumBudget) {
            CsvErrorWrapper cew = new CsvErrorWrapper("budget+expense", 0, "sumExpense > sumBudget", null);
            errorsPerFile.put("sumExpense > sumBudget", List.of(cew));
        }
    }

    private void setSuccessAttributes(RedirectAttributes redirectAttributes, List<Customer> customers, List<Budget> budgets, List<Expense> expenses) {
        redirectAttributes.addFlashAttribute("messageImp", "Mety doly dada 👌");
        redirectAttributes.addFlashAttribute("customersOk", customers.size());
        redirectAttributes.addFlashAttribute("budgetsOk", budgets.size());
        redirectAttributes.addFlashAttribute("expensesOk", expenses.size());
    }

    private void setErrorAttributes(Model model, HashMap<String, List<CsvErrorWrapper>> errorsPerFile) {
        model.addAttribute("customersErrors", errorsPerFile.get("customers"));
        model.addAttribute("budgetsErrors", errorsPerFile.get("budgets"));
        model.addAttribute("expensesErrors", errorsPerFile.get("expenses"));
    }

    private void saveBatches(List<Customer> customers, List<Budget> budgets, List<Expense> expenses) {
        int BATCH_SIZE = 100;
        // efa vita insert
//        customerService.saveBatch(customers, BATCH_SIZE);
        budgetService.saveBatch(budgets, BATCH_SIZE);
        expenseService.saveBatch(expenses, BATCH_SIZE);
    }

//    @PostMapping("/customer")
//    public String customer(
//            @RequestParam("file") MultipartFile file,
//            Authentication authentication,
//            RedirectAttributes redirectAttributes,
//            @RequestParam(required = false, defaultValue = "false") boolean sendEmail
//    ) {
//        try {
//            int userId = authenticationUtils.getLoggedInUserId(authentication);
//            User loggedInUser = userService.findById(userId);
//            if (loggedInUser.isInactiveUser()) {
//                return "error/account-inactive";
//            }
//
//            // import csv
//            List<Customer> customers = customerService.importCsv(file, loggedInUser, authentication, sendEmail);
//
//            String msg = "Fichier CSV traité avec succès : " + customers.size() + " lignes insérées";
//            redirectAttributes.addFlashAttribute("message", msg);
//        } catch (CsvValidationException e) {
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("errorImp", e.getErrors());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            redirectAttributes.addFlashAttribute("errorImp", ex.getMessage());
//        }
//        return "redirect:/data/management/izy";
//    }
//
//    @PostMapping("/expense")
//    public String expense(
//            @RequestParam("file") MultipartFile file,
//            Authentication authentication,
//            RedirectAttributes redirectAttributes
//    ) {
//        try {
//            int userId = authenticationUtils.getLoggedInUserId(authentication);
//            User loggedInUser = userService.findById(userId);
//            if (loggedInUser.isInactiveUser()) {
//                return "error/account-inactive";
//            }
//
//            // import csv
//            List<Expense> expenses = expenseService.importCsv(file, loggedInUser);
//
//            String msg = "Fichier CSV traité avec succès : " + expenses.size() + " lignes insérées";
//            redirectAttributes.addFlashAttribute("messageImp", msg);
//        } catch (CsvValidationException e) {
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("errorImp", e.getErrors());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            redirectAttributes.addFlashAttribute("errorImp", ex.getMessage());
//        }
//        return "redirect:/data/management/izy";
//    }
//
//    @PostMapping("/budget")
//    public String budget(
//            @RequestParam("file") MultipartFile file,
//            Authentication authentication,
//            RedirectAttributes redirectAttributes
//    ) {
//        try {
//            int userId = authenticationUtils.getLoggedInUserId(authentication);
//            User loggedInUser = userService.findById(userId);
//            if (loggedInUser.isInactiveUser()) {
//                return "error/account-inactive";
//            }
//
//            // import csv
//            List<Budget> expenses = budgetService.importCsv(file);
//
//            String msg = "Fichier CSV traité avec succès : " + expenses.size() + " lignes insérées";
//            redirectAttributes.addFlashAttribute("messageImp", msg);
//        } catch (CsvValidationException e) {
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("errorImp", e.getErrors());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            redirectAttributes.addFlashAttribute("errorImp", ex.getMessage());
//        }
//        return "redirect:/data/management/izy";
//    }
}
