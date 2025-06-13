package projekt.PD.Controller.Trainers_Controller;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.Services.CurrentUser;

import java.util.List;

@Controller
@RequestMapping("/trainer")
public class TrainerController {

    private final TrainerService trainerService;
    private final CurrentUser currentUser;

    public TrainerController(TrainerService trainerService, UserService userService, CurrentUser currentUser) {
        this.trainerService = trainerService;
        this.currentUser = currentUser;
    }

    /**
     * Obsługuje metodę GET, zwraca listę wszystkich trenerów
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return trainers
     */
    @GetMapping()
    public String getAllTrainers(Model model) {
        currentUser.addUserToModel(model);
        List<Trainer> trainers = trainerService.getAll();
        if(!trainers.isEmpty()){
            List<TrainerDTO> trainersDTO = TrainerDTO.toDTO(trainers);
            model.addAttribute("trainers", trainersDTO);
            return "Trainer/trainers";
        }

        model.addAttribute("error", "No trainers found");
        return "Trainer/trainers";
    }

    /**
     * Obsługuje metodę GET, zwrcaja stronę do zostania trenerem
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return become-trainer
     */
    @RolesAllowed({"USER",})
    @GetMapping("/form")
    public String getAddTrainerPage(Model model) {
        currentUser.addUserToModel(model);
        model.addAttribute("trainer", new Trainer());

        return "Trainer/become-trainer";
    }

    /**
     * Obsługuje metodę POST, tworzy użytkownika trenerem
     *
     * @param trainer klasa trainer
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return home
     */
    @PostMapping("/form")
    public String addTrainer(@ModelAttribute Trainer trainer, Model model) {
        User user = currentUser.getUserID();
        model.addAttribute("user", user);
        if (user.getTrainer() != null) {
            model.addAttribute("error", "User is already a trainer");
            return "error";
        }

        user.setRoles("ROLE_TRAINER");
        trainer.setUser(user);
        trainer.setId(null);
        trainer.setCourses(null);
        trainerService.createTrainer(trainer);

        model.addAttribute("message", "User is now a Trainer");
        return "User/Information/home";
    }

}
