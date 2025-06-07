package projekt.PD.Controller;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import jakarta.annotation.security.RolesAllowed;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerService;
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

    public UserController(UserService userService, User_WorkoutService userWorkoutService, TrainerService trainerService, UserTrainingPlanService userTrainingPlanService) {
        this.userService = userService;
        this.user_workoutService = userWorkoutService;
        this.trainerService = trainerService;
        this.userTrainingPlanService = userTrainingPlanService;
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
        public String getAllUser_Workouts(Model model) {
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
        public ResponseEntity<?> getUser_Workouts(@PathVariable Long id) {
            User user = getUserID();
            Optional<User_Workouts> uw = user_workoutService.findById(id, user.getId());
            if(uw.isPresent()){
                return new ResponseEntity<>(new WorkoutDTO(uw.get()),HttpStatus.OK);
            }

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // create workout 
        @PostMapping("/workout")
        public String updateWorkout(@RequestBody WorkoutDTO workoutDTO, Model model) {
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
        public ResponseEntity<?> deleteWorkout(@PathVariable Long id) {
            User user = getUserID();
            if(user_workoutService.deleteById(id,user.getId())){
                return new ResponseEntity<>("Workout deleted", HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>("Workout not found", HttpStatus.NOT_FOUND);
            }
        }

        // Plany Treningowe
        // Metody:
        //          -- Pobrannie wybranego planu (GET)
        //          -- Pobranie wszystkich planów (GET)
        //          -- Utworzenie lub modyfikacja planów (POST)
        //          -- Usunięcie planów (DEL)
        @GetMapping("/trainingplan")
        public String getAllUser_Trainers(Model model) {
            User user = getUserID();
            model.addAttribute("user", user);
            List<UserTrainingPlan> plan = userTrainingPlanService.findByUser_Id(user.getId());

            if(plan.isEmpty()){
                model.addAttribute("error", "No training plans found");
                return "user-training-plans";
            }

            List<UserTrainingPlanDTO> planDTO = UserTrainingPlanDTO.toDTO(plan);
            model.addAttribute("plans", planDTO);
            return "user-training-plans"; // Thymeleaf template for displaying training plans
        }

        @GetMapping("/trainingplan/{id}")
        public ResponseEntity<?> getAllUser_Trainers(@PathVariable Long id) {
            User user = getUserID();
            Optional<UserTrainingPlan> plan = userTrainingPlanService.findById(id,user.getId());
            if(plan.isPresent()){
                UserTrainingPlanDTO planDTO = new UserTrainingPlanDTO(plan.get());
                return new ResponseEntity<>(planDTO,HttpStatus.OK);
            }


            return new ResponseEntity<>("No Training plan with this ID",HttpStatus.NOT_FOUND);
        }

        @PostMapping("/trainingplan")
        public ResponseEntity<?> updateTrainingPlan(@RequestBody UserTrainingPlan plan) {
            User user = getUserID();
            plan.setUser(user);
            userTrainingPlanService.create_or_change(plan);

            return new ResponseEntity<>("Training plan created", HttpStatus.CREATED);
        }

        @DeleteMapping("/trainingplan/{id}")
        public ResponseEntity<?> deleteTrainingPlan(@PathVariable Long id) {
            User user = getUserID();
            if(userTrainingPlanService.deleteById(id,user.getId())){
                return new ResponseEntity<>("Training plan removed", HttpStatus.OK);
            }

            return new ResponseEntity<>("Training plan with id: "+id+ " not exist", HttpStatus.NOT_FOUND);
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
