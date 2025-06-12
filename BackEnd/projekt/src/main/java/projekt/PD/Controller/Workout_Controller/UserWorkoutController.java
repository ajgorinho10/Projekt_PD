package projekt.PD.Controller.Workout_Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/workout/user")
public class UserWorkoutController {
    private final CurrentUser currentUser;
    private final User_WorkoutService userWorkoutService;

    public UserWorkoutController(CurrentUser currentUser, User_WorkoutService userWorkoutService) {
        this.currentUser = currentUser;
        this.userWorkoutService = userWorkoutService;
    }

    /**
     * Obsługuje metode GET, pobiera wszystkie treningi użytkownika do wyświetlenia
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return user-workouts
     */
    @GetMapping()
    public String getThisUser_Workouts(Model model) {
        User user = currentUser.getUserID();
        model.addAttribute("user", user);

        List<User_Workouts> userWKOUT = userWorkoutService.findByUser_Id(user.getId());

        if(!userWKOUT.isEmpty()){
            List<WorkoutDTO> workoutDTOs = WorkoutDTO.toDTO(userWKOUT);
            model.addAttribute("workouts", workoutDTOs);

            return "WorkOut/User/user-workouts";
        }

        model.addAttribute("error", "No workouts found");
        return "WorkOut/User/user-workouts";
    }

    /**
     * Obsługuje metode GET, pobiera dany trening użytkownika do wyświetlenia
     *
     * @param id id treningu
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return user-workout-details
     */
    @GetMapping("/{id}")
    public String getUser_Workouts(@PathVariable Long id, Model model) {
        User user = currentUser.getUserID();

        Optional<User_Workouts> uw = userWorkoutService.findById(id, user.getId());

        if(uw.isPresent()){
            WorkoutDTO workoutDTO = new WorkoutDTO(uw.get());
            model.addAttribute("workout", workoutDTO);
            model.addAttribute("user", user);

            return "WorkOut/User/user-workout-details";
        }

        model.addAttribute("error", "Workout not found");
        return "WorkOut/User/user-workouts";
    }

    /**
     * Obsługuje metode GET, strona do tworzenia nowych treningów
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return create-workout
     */
    @GetMapping("/form")
    public String showCreateWorkoutForm(Model model) {
        model.addAttribute("workoutDTO", new WorkoutDTO());

        return "WorkOut/User/create-workout"; // Thymeleaf template
    }

    /**
     * Obsługuje metode POST, dodaje nowy trening użytkownika
     *
     * @param workoutDTO objekt przechowywujący informacje o nowym treningu
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return workout
     */
    @PostMapping("/form")
    public String updateWorkout(@ModelAttribute WorkoutDTO workoutDTO, Model model) {
        User user = currentUser.getUserID();

        User_Workouts user_workouts = new User_Workouts();
        user_workouts.setUser(user);
        user_workouts.setDescription(workoutDTO.getDescription());
        user_workouts.setDate(workoutDTO.getDate());
        user_workouts.setTitle(workoutDTO.getTitle());

        if(workoutDTO.getId() != null){
            user_workouts.setId(workoutDTO.getId());
        }
        userWorkoutService.createUser_Workouts(user_workouts);

        model.addAttribute("message", "Utworzono trening");
        model.addAttribute("user", user);

        return "redirect:/workout/user";
    }

    /**
     * Obsługuje metode POST, usuwa trening użytkownika
     *
     * @param id id treningu do usunięcia
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return user-workouts
     */
    @DeleteMapping("/{id}")
    public String deleteWorkout(@PathVariable Long id, Model model) {
        User user = currentUser.getUserID();

        if(userWorkoutService.deleteById(id,user.getId())){
            model.addAttribute("message", "Workout deleted successfully");
            return "redirect:/workout/user";
        }
        else{
            model.addAttribute("error", "Something went wrong");
            return "error";
        }
    }
}
