package projekt.PD.Controller.TrainerPlan_Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import org.springframework.ui.Model;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/trainerPlan/trainer")
public class TrainerPlanController {

    private final TrainerPlanService trainerPlanService;
    private final UserService userService;

    public TrainerPlanController(TrainerPlanService trainerPlanService, UserService userService) {
        this.trainerPlanService = trainerPlanService;
        this.userService = userService;
    }

    @GetMapping("/user")
    public String getTrainerPlan(Model model) {
        User user = getUserID();
        List<TrainerPlan> plans = trainerPlanService.findByTrainerPlanUser_Id(user.getId());
        if(plans!=null) {
            List<TrainerPlanDTO> trainerPlanDTOs = TrainerPlanDTO.toDTO(plans);
            model.addAttribute("plans", trainerPlanDTOs);
            model.addAttribute("user", user);
            return "TrainingPlan/Users/user-training-plans";

        }
        model.addAttribute("error", "No training plans found");
        return "TrainingPlan/Users/user-training-plans";

    }

    @GetMapping("/user/{id}")
    public String getUserTrainingPlan(@PathVariable Long id, Model model) {
        User user = getUserID();
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndTrainerPlanUser_Id(id,user.getId());
        if(plan.isPresent()) {
            model.addAttribute("plan", plan.get());
            model.addAttribute("user", user);
            return "TrainingPlan/Users/user-training-plan-details";
        }

        model.addAttribute("error", "Training plan not found");
        return "TrainingPlan/Users/user-training-plan-details";
    }

    @DeleteMapping("/user/{id}")
    public String deleteUserTrainingPlan(@PathVariable Long id, Model model) {
        User user = getUserID();
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndTrainerPlanUser_Id(id,user.getId());
        if(plan.isPresent()) {
            trainerPlanService.deleteById(plan.get().getId());
            model.addAttribute("user", user);
            model.addAttribute("success", "Training plan deleted successfully");
            return "TrainingPlan/Users/user-training-plans";
        }

        model.addAttribute("error", "Training plan not found");
        return "TrainingPlan/Users/user-training-plans";
    }



    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/trainer-courses")
    public String getPlanTrainer(Model model) {
        User user = getUserID();
        if(user.getTrainer()!=null) {
            List<TrainerPlan> plans = trainerPlanService.findByPlanTrainer_Id(user.getTrainer().getId());

            if(plans!=null) {
                model.addAttribute("user", user);
                model.addAttribute("plans", TrainerPlanDTO.toDTO(plans));
                return "TrainingPlan/Users/trainer-courses";
            }
        }

        model.addAttribute("error", "No training plans found");
        return "TrainingPlan/Users/trainer-courses";
    }

    // szczegóły planu treningowego (od???) trenera
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/trainer/{id}")
    public String getTrainerPlan(@PathVariable Long id, Model model) {
        User user = getUserID();
        if(user.getTrainer() != null) {
            Optional<TrainerPlan> plan = trainerPlanService.findByIdAndPlanTrainer_Id(id,user.getTrainer().getId());
            if(plan.isPresent()) {
                model.addAttribute("user", user);
                model.addAttribute("plan", plan.get());
                return "TrainingPlan/Users/user-training-plan-details";
            }
        }

        model.addAttribute("error", "Training plan not found");
        return "TrainingPlan/Users/trainer-courses";
    }

    // TODO
    // id użytkownika trzeba wpisywać ręcznie???
    // czy jest gdzieś funkcja wypisania wszystkich użytkowników aby na stronie dać listę/przypisać wielu naraz?

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/form")
    public String showTrainerPlanForm(Model model) {
        User trainer = getUserID();

        if (trainer.getTrainer() != null) {
            model.addAttribute("trainerPlanDTO", new TrainerPlanDTO());
            model.addAttribute("user", trainer);
            // No targetUser here, since trainer will provide id in form
            return "TrainingPlan/Users/create-training-plan";
        }

        model.addAttribute("error", "Can't show form for training plan creation");
        return "TrainingPlan/Users/user-training-plans";
    }


    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/trainer")
    public String createTrainerPlan(@ModelAttribute TrainerPlanDTO trainerPlanDTO,
                                    @RequestParam("targetUserId") Long targetUserId,
                                    Model model) {
        User trainer = getUserID();
                                    
        if(trainer.getTrainer() == null) {
            model.addAttribute("error", "You are not a trainer");
            return "TrainingPlan/Users/user-training-plans";
        }
    
        if(trainer.getId().equals(targetUserId)) {
            model.addAttribute("error", "You cannot assign a training plan to yourself.");
            model.addAttribute("user", trainer);
            return "TrainingPlan/Users/create-training-plan";
        }
    
        User user = userService.findUserById(targetUserId);
    
        if(user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("user", trainer);
            return "TrainingPlan/Users/create-training-plan";
        }
    
        TrainerPlan trainerPlan = new TrainerPlan(trainerPlanDTO);
        trainerPlan.setPlanTrainer(trainer.getTrainer());
        trainerPlan.setTrainerPlanUser(user);
    
        if(trainerPlanService.create(trainerPlan)){
            model.addAttribute("user", trainer);
            model.addAttribute("success", "Training plan created successfully");
            return "TrainingPlan/Users/user-training-plans";
        }
    
        model.addAttribute("error", "Failed to create training plan");
        model.addAttribute("user", trainer);
        return "TrainingPlan/Users/create-training-plan";
}

    

    @DeleteMapping("/trainer/{id}")
    public String deleteTrainerPlan(@PathVariable Long id, Model model) {
        User user = getUserID();
        if(user.getTrainer() == null) {
            model.addAttribute("error", "You are not a trainer");
            return "TrainingPlan/Users/user-training-plans";
        }
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndPlanTrainer_Id(id,user.getTrainer().getId());

        if(plan.isPresent()) {
            trainerPlanService.deleteById(plan.get().getId());
            model.addAttribute("user", user);
            model.addAttribute("success", "Training plan deleted successfully");
            return "TrainingPlan/Users/user-training-plans";
        }

        model.addAttribute("error", "Training plan not found");
        return "TrainingPlan/Users/user-training-plans";
    }


    //Funkcja pomocnicza do indentyfikowania uzytkownika
    private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }
}
