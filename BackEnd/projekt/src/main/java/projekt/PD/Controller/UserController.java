package projekt.PD.Controller;

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
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")

public class UserController {

    private final UserService userService;
    private final User_WorkoutService user_workoutService;
    private final TrainerService trainerService;

    public UserController(UserService userService, User_WorkoutService userWorkoutService, TrainerService trainerService) {
        this.userService = userService;
        user_workoutService = userWorkoutService;
        this.trainerService = trainerService;
    }

    @RolesAllowed("ADMIN")
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @RolesAllowed({"USER",})
    @PostMapping("/trainer/{id}")
    public ResponseEntity<User> addTrainer(@RequestBody Trainer trainer,@PathVariable int id) {
        if(userService.ifUserExists(id)){
            User user = userService.findUserById(id);
            user.setRoles("ROLE_TRAINER");
            trainer.setUser(user);

            trainerService.createTrainer(trainer);

            user = userService.findUserById(id);
            return new ResponseEntity<>(user,HttpStatus.CREATED);
        }

       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/trainer")
    public ResponseEntity<List<User>> getAllTrainers() {
        List<User> users = userService.getUsersByRole("ROLE_TRAINER");

        return new ResponseEntity<>(users,HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User user = getUserID();
        UserDTO userDTO = new UserDTO(user);

        return new ResponseEntity<>(userDTO, HttpStatus.OK);

    }

    @GetMapping("/workouts")
    public ResponseEntity<List<WorkoutDTO>> getAllUser_Workouts() {
        User user = getUserID();
        List<WorkoutDTO> userWKOUT = workoutToWorkoutDTO(user_workoutService.findByUser_Id(user.getId()));

        return new ResponseEntity<>(userWKOUT, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_TRAINER')")
    @PostMapping("/workouts/{userId}")
    public ResponseEntity<?> newWorkout(@RequestBody User_Workouts user_workouts, @PathVariable int userId) {
        Trainer trainer = getUserID().getTrainer();
        User user = userService.findUserById(userId);

        user_workouts.setUser(user);
        user_workouts.setTrainer(trainer);

        user_workoutService.createUser_Workouts(user_workouts);

        return new ResponseEntity<>("WorkOut created successfully", HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_TRAINER')")
    @DeleteMapping("/workouts/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable int id) {
        user_workoutService.deleteById(id);

        return new ResponseEntity<>("WorkOut deleted successfully", HttpStatus.OK);
    }

    //Funkcje pomocniczne do bezpiecznych odpowiedzi
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

    private List<TrainerDTO> trainerToUserDTO(List<Trainer> trainers) {
        List<TrainerDTO> trainerDTOs = new ArrayList<>();
        for (Trainer trainer : trainers) {
            UserDTO user = new UserDTO(trainer.getUser());
            trainerDTOs.add(new TrainerDTO(user));
        }

        return trainerDTOs;
    }

    private List<WorkoutDTO> workoutToWorkoutDTO(List<User_Workouts> workouts){
        List<WorkoutDTO> workoutDTOs = new ArrayList<>();
        for (User_Workouts user_workout : workouts) {
            UserDTO user = new UserDTO(user_workout.getUser());
            TrainerDTO trainerDTO = new TrainerDTO(user);

            workoutDTOs.add(new WorkoutDTO(user_workout,trainerDTO,user));
        }

        return workoutDTOs;
    }
}
