package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.service.DataManagementService;

@Controller
@RequestMapping("/data/management")
@RequiredArgsConstructor
public class DataManagementController {

    private final DataManagementService service;

    @GetMapping("/clear")
    public String showPage(Model model) {
        model.addAttribute("tables", service.getDeletableTables());
        return "/data-management/clear";
    }

    @PostMapping("/clear-all")
    public String clearAllTables(RedirectAttributes redirectAttributes) {
        try {
            service.deleteAllTables();
            redirectAttributes.addFlashAttribute("message", "Toutes les tables ont été vidées avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/data/management/clear";
    }

    @PostMapping("/clear-table")
    public String clearTable(@RequestParam(name = "tableName") String tableName, RedirectAttributes redirectAttributes) {
        try {
            service.deleteTable(tableName);
            redirectAttributes.addFlashAttribute("message", "La table " + tableName + " a été vidée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/data/management/clear";
    }
}
