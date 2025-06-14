package projekt.PD.Controller.TrainerPlan_Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

/*
 * Klasa TrainerPlanController obsługuje żądania wyświetlania, tworzenia i usuwania planów
 *  treningowych tworzonych przez trenera 
 */

@Controller
@RequestMapping("/trainerplan/trainer")
public class TrainerPlanController {

    private final TrainerPlanService trainerPlanService;
    private final UserService userService;
    private final CurrentUser currentUser;

    public TrainerPlanController(TrainerPlanService trainerPlanService, UserService userService, CurrentUser currentUser) {
        this.trainerPlanService = trainerPlanService;
        this.userService = userService;
        this.currentUser = currentUser;
    }



    /**
     * Obsługuje żądanie GET w celu wyświetlenia wszystkich treningów utworzonych przez trenera dla użytkowników
     * 
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return trainer-traning-plan
     */

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping()
    public String getPlanTrainer(Model model) {
        User user = currentUser.getUserID();
        model.addAttribute("user", user);

        assert user.getTrainer() != null;
        List<TrainerPlanDTO> planDTOS = TrainerPlanDTO.toDTO(trainerPlanService.findByPlanTrainer_Id(user.getTrainer().getId()));
        if(planDTOS.isEmpty()){
            model.addAttribute("msg", "Brak planów od trenera");
        }

        model.addAttribute("plans", planDTOS);

        return "TrainingPlanByTrainer/Trainer/trainer-traning-plan";
    }


    /**
     * Obsługuje żądanie GET w celu wyświetlenia formularza do tworzenia nowego planu treningowego dla użytkowników
     * 
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return add-training-plan-to-user
     */

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/form")
    public String newTrainingPlanForUser(Model model) {
        User user = currentUser.getUserID();
        model.addAttribute("user", user);
        model.addAttribute("trainerPlanDTO", new TrainerPlanDTO());

        return "TrainingPlanByTrainer/Trainer/add-training-plan-to-user";
    }

    /**
     * Obsługuje żądanie POST w celu przesłania danych z formularza i utworzenia nowego planu treningowego
     * 
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @param trainerPlanDTO obiekt zawierający dane planu treningowego
     * @return redirect:/trainerplan/trainer - przekierowanie na stronę podglądu planów treningowych trenera
     */


    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/form")
    public String createTrainingPlanForUser(TrainerPlanDTO trainerPlanDTO,Model model) {
        User user = currentUser.getUserID();
        User userToPlan = userService.findUserById(trainerPlanDTO.getUser().getId());

        if(user.getTrainer()!=null && userToPlan != null){
            TrainerPlan trainerPlan = new TrainerPlan(trainerPlanDTO);
            trainerPlan.setPlanTrainer(user.getTrainer());
            trainerPlan.setTrainerPlanUser(userToPlan);

            if(trainerPlanService.create(trainerPlan)){
                model.addAttribute("msg","Stworzono nowy plan");
                System.out.println("Stworzono nowy plan");

                return "redirect:/trainerplan/trainer";
            }
            System.out.println("błąd");
        }


        return "TrainingPlanByTrainer/Trainer/add-training-plan-to-user";
    }


    /**
     * Obsługuje żądanie DELETE usuwające wybrany plan treningowy, w przupadku błędu wyświetla komunikat o błędzie
     * 
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @param id id planu treningowego do usunięcia
     * @return redirect:/trainerplan/trainer - przekierowanie na stronę podglądu planów treningowych trenera
     */

    @PreAuthorize("hasRole('TRAINER')")
    @DeleteMapping("/{id}")
    public String deleteTrainingPlan(@PathVariable Long id, Model model) {
        User user = currentUser.getUserID();
        model.addAttribute("user", user);
        assert user.getTrainer() != null;
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndPlanTrainer_Id(id,user.getTrainer().getId());

        if(plan.isPresent() && trainerPlanService.deleteById(plan.get().getId())) {
            model.addAttribute("msg","Usunięto plan");
        }
        else {
            model.addAttribute("msg","Błąd podczas usuwania planu");
        }

        return "redirect:/trainerplan/trainer";
    }
}
