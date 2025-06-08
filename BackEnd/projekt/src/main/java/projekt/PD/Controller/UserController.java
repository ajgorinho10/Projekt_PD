package projekt.PD.Controller;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.annotation.security.RolesAllowed;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerService;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanDTO;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanService;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.DataBase.PD_Course.Course;

@Controller
@RequestMapping("/users")

public class UserController {

    private final UserService userService;
    private final User_WorkoutService user_workoutService;
    private final TrainerService trainerService;
    private final UserTrainingPlanService userTrainingPlanService;
    private final TrainerPlanService trainerPlanService;

    public UserController(UserService userService, User_WorkoutService userWorkoutService, TrainerService trainerService, UserTrainingPlanService userTrainingPlanService, TrainerPlanService trainerPlanService) {
        this.userService = userService;
        this.user_workoutService = userWorkoutService;
        this.trainerService = trainerService;
        this.userTrainingPlanService = userTrainingPlanService;
        this.trainerPlanService = trainerPlanService;
    }

    // route /home został przeniesiony do AuthPageController

    // jak zostac trenerem

    @RolesAllowed({"USER",})

    @GetMapping("/trainer")
    public String getAddTrainerPage(Model model) {
        addUserToModel(model);            // for name display
        model.addAttribute("trainer", new Trainer());   // for form binding
        return "become-trainer";
    }





    @PostMapping("/trainer")
    public String addTrainer(@ModelAttribute Trainer trainer, Model model) {
        User user = getUserID();
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
        return "home";
    }


    // Pobieranie listy trenerów
    @GetMapping("/trainers")
    public String getAllTrainers(Model model) {
        addUserToModel(model);
        List<Trainer> trainers = trainerService.getAll();
        if(!trainers.isEmpty()){
            List<TrainerDTO> trainersDTO = TrainerDTO.toDTO(trainers);
            model.addAttribute("trainers", trainersDTO);
            return "trainers"; // Thymeleaf template
        }

        model.addAttribute("error", "No trainers found");
        return "trainers"; // Thymeleaf template
    }

        // Pobieranie informacji o sobie

        @GetMapping("/me")
        public String getCurrentUser(Model model) {
            addUserToModel(model);
            /* User user = getUserID();
            UserDTO userDTO = new UserDTO(user);
            model.addAttribute("user", userDTO); */
            return "about-me"; // Thymeleaf template

        }


        // Treningi użytkownika
        // METODY:
        //          -- pobranie wszystkich treningów (GET)
        //          -- pobranie wybranego treningu (GET)
        //          -- utworzenie lub modyfikacja treniengu (POST)
        //          -- usunięcie treningu (DEL)

        @GetMapping("/workout")
        public String getThisUser_Workouts(Model model) {
            User user = getUserID();
            model.addAttribute("user", user);
            List<User_Workouts> userWKOUT = user_workoutService.findByUser_Id(user.getId());
            if(!userWKOUT.isEmpty()){
                List<WorkoutDTO> workoutDTOs = WorkoutDTO.toDTO(userWKOUT);
                model.addAttribute("workouts", workoutDTOs);
                return "user-workouts"; // Thymeleaf template
            }

            model.addAttribute("error", "No workouts found");
            return "user-workouts"; // Thymeleaf template
        }

        @GetMapping("/workout/{id}")
        public String getUser_Workouts(@PathVariable Long id, Model model) {
            User user = getUserID();
            Optional<User_Workouts> uw = user_workoutService.findById(id, user.getId());
            if(uw.isPresent()){
                WorkoutDTO workoutDTO = new WorkoutDTO(uw.get());
                model.addAttribute("workout", workoutDTO);
                model.addAttribute("user", user);
                return "user-workout-details"; // Thymeleaf template for displaying workout details
            }

            model.addAttribute("error", "Workout not found");
            return "user-workout-details"; // Redirect to the workouts page if not found
        }


        // forma do tworzenia treningu
        @GetMapping("/workout/form")
        public String showCreateWorkoutForm(Model model) {
        model.addAttribute("workoutDTO", new WorkoutDTO());
        return "create-workout"; // Thymeleaf template
        }

        // create workout 
        @PostMapping("/workout")
        public String updateWorkout(@ModelAttribute WorkoutDTO workoutDTO, Model model) {
            User user = getUserID();

            User_Workouts user_workouts = new User_Workouts();
            user_workouts.setUser(user);
            user_workouts.setDescription(workoutDTO.getDescription());
            user_workouts.setDate(workoutDTO.getDate());
            user_workouts.setTitle(workoutDTO.getTitle());

            if(workoutDTO.getId() != null){
                user_workouts.setId(workoutDTO.getId());
            }
            user_workoutService.createUser_Workouts(user_workouts);

            model.addAttribute("message", "Utworzono trening");
            model.addAttribute("user", user);
            return "redirect:/users/workout"; // Redirect to the workouts page after creation
        }

        @DeleteMapping("/workout/{id}")
        public String deleteWorkout(@PathVariable Long id, Model model) {
            User user = getUserID();
            if(user_workoutService.deleteById(id,user.getId())){
                model.addAttribute("message", "Workout deleted successfully");
                return "redirect:/users/workout"; // Redirect to the workouts page after deletion
            }
            else{
                model.addAttribute("error", "Something went wrong");
                return "error"; // Redirect to an error page if the workout was not found
            }
        }

        // Plany Treningowe
        // Metody:
        //          -- Pobrannie wybranego planu (GET)
        //          -- Pobranie wszystkich planów (GET)
        //          -- Utworzenie lub modyfikacja planów (POST)
        //          -- Usunięcie planów (DEL)

        @GetMapping("/trainingplan")
        public String getAllUser_s(Model model) {

            User user = getUserID();
            model.addAttribute("user", user);
            model.addAttribute("trainerPlanDTO", new TrainerPlanDTO());

            List<TrainerPlan> plans = trainerPlanService.findByTrainerPlanUser_Id(user.getId());

            if (plans.isEmpty()) {
                model.addAttribute("error", "No training plans found");
                return "user-training-plans";
            }

            List<TrainerPlanDTO> planDTO = TrainerPlanDTO.toDTO(plans);
            model.addAttribute("plans", planDTO);
            return "user-training-plans";
        }


        @GetMapping("/trainingplan/{id}")
        public String getAllUser_Trainers(@PathVariable Long id, Model model) {
            User user = getUserID();
            Optional<UserTrainingPlan> plan = userTrainingPlanService.findById(id,user.getId());
            if(plan.isPresent()){
                UserTrainingPlanDTO planDTO = new UserTrainingPlanDTO(plan.get());
                model.addAttribute("plan", planDTO);
                model.addAttribute("user", user);
                return "user-training-plan-details"; // Thymeleaf template for displaying training plan details
            }


            model.addAttribute("error", "Training plan not found");
            return "user-training-plan-details"; // Redirect to the training plans page if not found
        }


        @GetMapping("/trainingplan/form")
        public String showCreateTrainingPlanForm(Model model) {
            User user = getUserID();
            model.addAttribute("user", user);
            model.addAttribute("trainerPlanDTO", new TrainerPlanDTO());
            return "create-training-plan";
        }


        @PostMapping("/trainingplan")
        public String updateTrainingPlan(@ModelAttribute UserTrainingPlan plan) {
            User user = getUserID();
            plan.setUser(user);
            userTrainingPlanService.create_or_change(plan);

            return "redirect:/users/trainingplan"; // Redirect to the training plans page after creation
        }

        @DeleteMapping("/trainingplan/{id}")
        public String deleteTrainingPlan(@PathVariable Long id, Model model) {
            User user = getUserID();
            if(userTrainingPlanService.deleteById(id,user.getId())){
                model.addAttribute("message", "Training has been deleted");
                return "redirect:/users/trainingplan"; // Redirect to the training plans page after deletion
            }
            model.addAttribute("error", "There was a problem when deleting the training plan");
           return "redirect:/users/trainingplan";
        }





    private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }

    private void addUserToModel(Model model) {
        User user = getUserID();
        model.addAttribute("user", user);
    }
}
