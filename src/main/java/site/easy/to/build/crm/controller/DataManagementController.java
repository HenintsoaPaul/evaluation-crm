package site.easy.to.build.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.google.service.acess.GoogleAccessService;
import site.easy.to.build.crm.service.DataDeleteService;
import site.easy.to.build.crm.service.DataGeneratorService;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;

@Controller
@RequestMapping("/data/management")
@RequiredArgsConstructor
public class DataManagementController {

    private final DataDeleteService service;
    private final DataGeneratorService generatorService;
    private final AuthenticationUtils authenticationUtils;
    private final UserServiceImpl userService;

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

    @GetMapping("/izy")
    public String showFichier3(Model model, Authentication authentication) {
        // security role
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if(user.isInactiveUser()) {
            return "error/account-inactive";
        }

        // envoi email
        boolean hasGoogleGmailAccess = false;
        boolean isGoogleUser = false;
        if(!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            isGoogleUser = true;
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            if(oAuthUser.getGrantedScopes().contains(GoogleAccessService.SCOPE_GMAIL)){
                hasGoogleGmailAccess = true;
            }
        }

        model.addAttribute("hasGoogleGmailAccess", hasGoogleGmailAccess);
        model.addAttribute("isGoogleUser", isGoogleUser);
        return "/data-management/csv";
    }
}
