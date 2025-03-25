package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.service.DataDeleteService;
import site.easy.to.build.crm.service.DataGeneratorService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/data/management")
@RequiredArgsConstructor
public class DataManagementController {

    private final DataDeleteService service;
    private final DataGeneratorService generatorService;

    @GetMapping
    public String showPage(Model model) {
        model.addAttribute("tables", service.getDeletableTables());
        model.addAttribute("tablesGen", service.getAllTables());
        return "/data-management/index";
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
        return "redirect:/data/management";
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
        return "redirect:/data/management";
    }

    @PostMapping("/gen-table")
    public String genDataForTable(
            @RequestParam String tableName,
            @RequestParam int nbRows,
            RedirectAttributes redirectAttributes
    ) {
        try {
            generatorService.genData(tableName, nbRows);

            redirectAttributes.addFlashAttribute("messageGen", nbRows + " ont ete inseres.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorGen", e.getMessage());
        }
        return "redirect:/data/management";
    }
}
