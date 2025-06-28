package projekt.PD.Controller.User_Controller;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.security.RolesAllowed;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerService;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanDTO;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanService;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.Services.CurrentUser;
import projekt.PD.Util.TotpUtil;


/*
 * Klasa UserController obsługuje żądania wyświetlania informacji o użytkowniku i wyświetlania strony głównej
 */
@Controller
public class UserController {

    private static final String ISSUER = "Banking System with TOTP";
    private final CurrentUser currentUser;
    private final UserService userService;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.currentUser = currentUser;
        this.userService = userService;
    }


    /**
     * Obsługuje metodę GET, Zwraca informacje o użytkowniku
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return about-me
     */
    @GetMapping("/me")
    public String getCurrentUser(Model model) {
        currentUser.addUserToModel(model);

        return "User/Information/about-me";

    }

    /**
     * Obsługuje metodę GET, zwraca strone główną po zalogowaniu
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return home
     */
    @GetMapping("/home")
    public String home(Model model) {
        User user = currentUser.getUserID();
        model.addAttribute("user", user);

        return "User/Information/home";
    }

    /**
     * Wyświetla stronę konfiguracji TOTP.
     */
    @GetMapping("/totp-setup")
    public String showTotpSetupPage(Authentication authentication, Model model, @RequestParam(value = "status", required = false) String status) {
        User user = currentUser.getUserID();
        currentUser.addUserToModel(model);
        String secret;
        String uri;
        String qrCode;

        if (user.isMfaEnabled()) {
            secret = user.getMfaSecret();
            model.addAttribute("msg", "Totp jest aktywowane");
        }
        else{
            secret = userService.generateMfaSecret(user.getId());
        }

        uri = TotpUtil.generateTotpUri(ISSUER, user.getLogin(), secret);

        qrCode = TotpUtil.generateQrCode(uri);

        model.addAttribute("username", user.getLogin());
        model.addAttribute("qrCodeImage", qrCode);
        model.addAttribute("mfaSecret", secret);
        model.addAttribute("TotpEnabled", user.isMfaEnabled());

        if(status != null) {
            if (status.equals("success")) {
                model.addAttribute("msg", "Pomyślnie aktywowano Totp");
            } else if (status.equals("error")) {
                model.addAttribute("msg", "Błąd podczas aktywacji Totp");
            }
        }

        return "Totp/totp-setup";
    }

    /**
     * Obsługuje weryfikację kodu TOTP i aktywację 2FA.
     */
    @PostMapping("/totp-setup")
    public String processTotpSetup(Authentication authentication, @RequestParam("totpCode") String totpCode, Model model) {

        User user = currentUser.getUserID();

        if (userService.verifyAndEnableMfa(user.getId(), totpCode)) {
            return "redirect:/totp-setup?status=success";
        } else {
            return "redirect:/totp-setup?status=error";
        }
    }

    /**
     * Obsługuje dezaktywację 2FA.
     */
    @PostMapping("/totp-disable")
    public String disableTotp(Authentication authentication) {
        User user = currentUser.getUserID();

        if (userService.disableMfa(user.getId())) {
            return "redirect:/totp-setup";
        } else {
            return "redirect:/home";
        }
    }

}
