package projekt.PD.Controller.TrainingPlan_Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanDTO;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanService;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/trainingplan/user")
public class UserTrainingPlanController {

    private final CurrentUser currentUser;
    private final UserTrainingPlanService userTrainingPlanService;

    public UserTrainingPlanController(CurrentUser currentUser, UserTrainingPlanService userTrainingPlanService) {
        this.currentUser = currentUser;
        this.userTrainingPlanService = userTrainingPlanService;
    }

    // Plany Treningowe
    // Metody:
    //          -- Pobrannie wybranego planu (GET)
    //          -- Pobranie wszystkich planów (GET)
    //          -- Utworzenie lub modyfikacja planów (POST)
    //          -- Usunięcie planów (DEL)

    @GetMapping()
    public String getAllUser_s(Model model) {

        User user = currentUser.getUserID();
        model.addAttribute("user", user);
        List<UserTrainingPlan> plans = userTrainingPlanService.findByUser_Id(user.getId());

        if (plans.isEmpty()) {
            model.addAttribute("error", "No training plans found");
            return "TrainingPlan/Users/user-training-plans";
        }

        List<UserTrainingPlanDTO> planDTO = UserTrainingPlanDTO.toDTO(plans);
        model.addAttribute("plans", planDTO);
        return "TrainingPlan/Users/user-training-plans";
    }


    @GetMapping("/{id}")
    public String getAllUser_Trainers(@PathVariable Long id, Model model) {
        User user = currentUser.getUserID();
        Optional<UserTrainingPlan> plan = userTrainingPlanService.findById(id,user.getId());

        if(plan.isPresent()){
            UserTrainingPlanDTO planDTO = new UserTrainingPlanDTO(plan.get());
            model.addAttribute("plan", planDTO);
            model.addAttribute("user", user);

            return "TrainingPlan/Users/user-training-plan-details";
        }

        model.addAttribute("error", "Training plan not found");

        return "TrainingPlan/Users/user-training-plan-details";
    }


    @GetMapping("/form")
    public String showCreateTrainingPlanForm(Model model) {
        User user = currentUser.getUserID();
        model.addAttribute("user", user);
        model.addAttribute("trainerPlanDTO", new UserTrainingPlanDTO());

        return "TrainingPlan/Users/create-training-plan";
    }


    @PostMapping("/form")
    public String updateTrainingPlan(@ModelAttribute UserTrainingPlan plan,Model model) {
        User user = currentUser.getUserID();

        plan.setUser(user);
        userTrainingPlanService.create_or_change(plan);

        return "redirect:/trainingplan/user";
    }

    @DeleteMapping("/{id}")
    public String deleteTrainingPlan(@PathVariable Long id, Model model) {
        System.out.println("Delete training plan:"+id);
        User user = currentUser.getUserID();

        if(userTrainingPlanService.deleteById(id,user.getId())){
            model.addAttribute("message", "Training has been deleted");
        }
        else {
            model.addAttribute("error", "There was a problem when deleting the training plan");
        }

        return "redirect:/trainingplan/user";
    }
}
