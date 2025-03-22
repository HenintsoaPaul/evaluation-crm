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
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.util.List;

@Controller
@RequestMapping("/import/csv")
@RequiredArgsConstructor
public class CsvController {

    private final UserServiceImpl userService;
    private final AuthenticationUtils authenticationUtils;

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

    @PostMapping
    public String genericImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName,
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

            String msg;
            switch (tableName) {
                case "user":
                    userService.importCsv(file, oAuthUser);
                    msg = "Import dans la table user!";
                    break;
                default:
                    msg = "Import dans la table user!";
                    break;
            }
            redirectAttributes.addFlashAttribute("messageImp", msg);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorImp", e.getMessage());
        }

        return "redirect:/data/management";
    }
}
