package projekt.PD.Controller.Api;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerService;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanDTO;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")

/** Kontroler Rest API do obsługi żądań związanych z aktualnie zalogowanym użytkownikiem */

public class UserRestController {

    private final UserService userService;
    private final User_WorkoutService user_workoutService;
    private final TrainerService trainerService;
    private final UserTrainingPlanService userTrainingPlanService;

    public UserRestController(UserService userService, User_WorkoutService userWorkoutService, TrainerService trainerService, UserTrainingPlanService userTrainingPlanService) {
        this.userService = userService;
        this.user_workoutService = userWorkoutService;
        this.trainerService = trainerService;
        this.userTrainingPlanService = userTrainingPlanService;
    }

    @RolesAllowed({"ADMIN"})
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Stawienie siebie jako trener
    // GIT - zostawiamy
    @RolesAllowed({"USER",})
    @PostMapping("/trainer/{id}")
    public ResponseEntity<?> addTrainer(@RequestBody Trainer trainer,@PathVariable Long id) {
        if(userService.ifUserExists(id)){

            User user = userService.findUserById(id);
            if(user.getTrainer() != null){
                return new ResponseEntity<>("User is already trainer",HttpStatus.CONFLICT);
            }
            user.setRoles("ROLE_TRAINER");
            trainer.setUser(user);
            trainer.setId(null);
            trainer.setCourses(null);
            trainerService.createTrainer(trainer);

            return new ResponseEntity<>("User is now Trainer",HttpStatus.CREATED);
        }

       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Pobieranie listy trenerów
    @GetMapping("/trainer")
    public ResponseEntity<?> getAllTrainers() {
        List<Trainer> trainers = trainerService.getAll();
        if(!trainers.isEmpty()){
            return new ResponseEntity<>(TrainerDTO.toDTO(trainers), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Pobieranie informacji o sobie
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User user = getUserID();
        UserDTO userDTO = new UserDTO(user);

        return new ResponseEntity<>(userDTO, HttpStatus.OK);

    }

    // Treningi użytkownika
    // METODY:
    //          -- pobranie wszystkich treningów (GET)
    //          -- pobranie wybranego treningu (GET)
    //          -- utworzenie lub modyfikacja treniengu (POST)
    //          -- usunięcie treningu (DEL)

    @GetMapping("/workout")
    public ResponseEntity<?> getAllUser_Workouts() {
        User user = getUserID();
        List<User_Workouts> userWKOUT = user_workoutService.findByUser_Id(user.getId());
        if(!userWKOUT.isEmpty()){
            return new ResponseEntity<>(userWKOUT,HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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

    @PostMapping("/workout")
    public ResponseEntity<?> updateWorkout(@RequestBody WorkoutDTO workoutDTO) {
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


        return new ResponseEntity<>("Workout created", HttpStatus.CREATED);
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
    public ResponseEntity<?> getAllUser_Trainers() {
        User user = getUserID();
        List<UserTrainingPlan> plan = userTrainingPlanService.findByUser_Id(user.getId());

        if(plan.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<UserTrainingPlanDTO> planDTO = UserTrainingPlanDTO.toDTO(plan);
        return new ResponseEntity<>(planDTO, HttpStatus.OK);
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


    //Funkcja pomocniczne do indentyfikowania uzytkownika
    private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }

}
