package projekt.PD.Controller.User_Controller;

import java.util.List;
import java.util.Optional;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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


/*
 * Klasa UserController obsługuje żądania wyświetlania informacji o użytkowniku i wyświetlania strony głównej
 */
@Controller
public class UserController {

    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.currentUser = currentUser;
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
}
