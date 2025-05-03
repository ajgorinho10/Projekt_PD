package projekt.PD.Controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.Null;
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
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
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

    @RolesAllowed("ADMIN")
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Stawienie siebie jako trener
    // GIT - zostawiamy
    @RolesAllowed({"USER",})
    @PostMapping("/trainer/{id}")
    public ResponseEntity<User> addTrainer(@RequestBody Trainer trainer,@PathVariable Long id) {
        if(userService.ifUserExists(id)){

            User user = userService.findUserById(id);
            user.setRoles("ROLE_TRAINER");
            trainer.setUser(user);
            trainer.setId(null);
            trainerService.createTrainer(trainer);

            return new ResponseEntity<>(user,HttpStatus.CREATED);
        }

       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Pobieranie listy trenerów
    @GetMapping("/trainer")
    public ResponseEntity<List<TrainerDTO>> getAllTrainers() {
        List<TrainerDTO> trainers = trainerToTrainerDTO(trainerService.getAll());


        return new ResponseEntity<>(trainers,HttpStatus.OK);
    }

    // Pobieranie informacji o sobie
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User user = getUserID();

        return new ResponseEntity<>(user, HttpStatus.OK);

    }

    // Treningi użytkownika
    // METODY:
    //          -- pobranie wszystkich treningów (GET)
    //          -- pobranie wybranego treningu (GET)
    //          -- utworzenie lub modyfikacja treniengu (POST)
    //          -- usunięcie treningu (DEL)

    @GetMapping("/workout")
    public ResponseEntity<List<WorkoutDTO>> getAllUser_Workouts() {
        User user = getUserID();
        List<WorkoutDTO> userWKOUT = userWorkoutToWorkoutDTO(user_workoutService.findByUser_Id(user.getId()));

        return new ResponseEntity<>(userWKOUT, HttpStatus.OK);
    }

    @GetMapping("/workout/{id}")
    public ResponseEntity<?> getUser_Workouts(@PathVariable Long id) {
        User user = getUserID();
        Optional<User_Workouts> uw = user_workoutService.findById(id, user.getId());
        if(uw.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(uw,HttpStatus.OK);
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

        return new ResponseEntity<>(plan, HttpStatus.OK);
    }

    @GetMapping("/trainingplan/{id}")
    public ResponseEntity<?> getAllUser_Trainers(@PathVariable Long id) {
        User user = getUserID();
        Optional<UserTrainingPlan> plan = userTrainingPlanService.findById(id,user.getId());
        if(plan.isEmpty()){
            return new ResponseEntity<>("No Training plan with this ID",HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(plan, HttpStatus.OK);
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



    //Funkcje pomocniczne do bezpiecznych odpowiedzi (DTO)
    private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }

    private List<UserDTO> userToUserDTO(List<User> users) {
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            userDTOs.add(new UserDTO(user));
        }

        return userDTOs;
    }

    private List<TrainerDTO> trainerToTrainerDTO(List<Trainer> trainers) {
        List<TrainerDTO> trainerDTOs = new ArrayList<>();
        for (Trainer trainer : trainers) {
            trainerDTOs.add(new TrainerDTO(trainer.getUser()));
        }

        return trainerDTOs;
    }

    private List<WorkoutDTO> userWorkoutToWorkoutDTO(List<User_Workouts> workouts){
        List<WorkoutDTO> workoutDTOs = new ArrayList<>();
        for (User_Workouts user_workout : workouts) {
            workoutDTOs.add(new WorkoutDTO(user_workout));
        }

        return workoutDTOs;
    }
}
