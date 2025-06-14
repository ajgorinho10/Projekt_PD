package projekt.PD.Controller.TrainerPlan_Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

/**
 * Klasa UserTrainerPlanController obsługuje żądania wyświetlania, usuwania planów treningowych przypisanych do użytkownika
 */

@Controller
@RequestMapping("/trainerplan/user")
public class UserTrainerPlanController {

    private final TrainerPlanService trainerPlanService;
    private final UserService userService;
    private final CurrentUser currentUser;

    public UserTrainerPlanController(TrainerPlanService trainerPlanService, UserService userService, CurrentUser currentUser) {
        this.trainerPlanService = trainerPlanService;
        this.userService = userService;
        this.currentUser = currentUser;
    }


    /**
     * Obsługuje żądanie GET wyświetlające wszystkie plany treningowe przypisane do użytkownika, w przypadku braku
     *  planów wyświetla komunikat o błędzie
     * 
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return user-training-plan-from-trainer
     */
    @GetMapping()
    public String getTrainerPlan(Model model) {
        User user = currentUser.getUserID();
        model.addAttribute("user", user);

        List<TrainerPlan> plans = trainerPlanService.findByTrainerPlanUser_Id(user.getId());

        if(!plans.isEmpty()) {
            model.addAttribute("plans", TrainerPlanDTO.toDTO(plans));
        }
        else{
            model.addAttribute("error", "Brak planów");
        }

        return "TrainingPlanByTrainer/User/user-training-plan-from-trainer";
    }


    /**
     * Obsługuje żądanie GET wyświetlające szczegóły planu treningowego przypisanego do użytkownika, w przypadku błędu
     *  wyświetla komunikat o o błędzie
     * 
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @param id id planu treningowego do podglądu
     * @return user-training-plan-from-trainer-details
     */
    @GetMapping("/{id}")
    public String getUserTrainingPlan(@PathVariable Long id,Model model) {
        User user = currentUser.getUserID();
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndTrainerPlanUser_Id(id,user.getId());
        if(plan.isPresent()) {
            model.addAttribute("plan",new TrainerPlanDTO(plan.get()));
        }
        else{
            model.addAttribute("msg","Błąd wyświetlania planu");
        }

        return "TrainingPlanByTrainer/User/user-training-plan-from-trainer-details";
    }

    /**
     * Obsługuje żądanie DELETE usuwające plan treningowy przypisany do użytkownika, w przypadku błędu wyświetla
     *  komunikat o błędzie
     * 
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @param id id planu treningowego do usunięcia
     * @redirect:/trainerplan/user - przekierowanie na stronę podglądu planów treningowych użytkownika
     */

    @DeleteMapping("/{id}")
    public String deleteUserTrainingPlan(@PathVariable Long id,Model model) {
        User user = currentUser.getUserID();
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndTrainerPlanUser_Id(id,user.getId());


        if(plan.isPresent() && trainerPlanService.deleteById(plan.get().getId())) {
            model.addAttribute("msg","Usunięto plan");
        }
        else{
            model.addAttribute("msg","Błąd usuwania planu");
        }

        return "redirect:/trainerplan/user";
    }
}
