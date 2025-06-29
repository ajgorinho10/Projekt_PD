package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import projekt.PD.Controller.Workout_Controller.UserWorkoutController;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;
import projekt.PD.Services.CurrentUser;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserWorkoutControllerTest {

    @InjectMocks
    private UserWorkoutController controller;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private User_WorkoutService workoutService;

    @Mock
    private Model model;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        when(currentUser.getUserID()).thenReturn(mockUser);
    }

    // GET /workout/user
    @Test
    void getThisUser_Workouts_ShouldReturnWorkoutsView_WhenWorkoutsExist() {
        List<User_Workouts> workouts = List.of(new User_Workouts());
        when(workoutService.findByUser_Id(mockUser.getId())).thenReturn(workouts);

        String view = controller.getThisUser_Workouts(model);

        verify(model).addAttribute(eq("workouts"), anyList());
        verify(model).addAttribute("user", mockUser);
        assertEquals("WorkOut/User/user-workouts", view);
    }

    @Test
    void getThisUser_Workouts_ShouldReturnErrorMessage_WhenNoWorkouts() {
        when(workoutService.findByUser_Id(mockUser.getId())).thenReturn(Collections.emptyList());

        String view = controller.getThisUser_Workouts(model);

        verify(model).addAttribute("error", "No workouts found");
        assertEquals("WorkOut/User/user-workouts", view);
    }


    @Test
    void getUser_Workouts_ShouldReturnWorkoutDetails_WhenWorkoutExists() {
        User_Workouts workout = new User_Workouts();
        workout.setId(1L);
        workout.setTitle("Test");
        Optional<User_Workouts> optionalWorkout = Optional.of(workout);
        when(workoutService.findById(1L, mockUser.getId())).thenReturn(optionalWorkout);

        String view = controller.getUser_Workouts(1L, model);

        verify(model).addAttribute(eq("workout"), any(WorkoutDTO.class));
        verify(model).addAttribute("user", mockUser);
        assertEquals("WorkOut/User/user-workout-details", view);
    }

    @Test
    void getUser_Workouts_ShouldReturnError_WhenWorkoutNotFound() {
        when(workoutService.findById(1L, mockUser.getId())).thenReturn(Optional.empty());

        String view = controller.getUser_Workouts(1L, model);

        verify(model).addAttribute("error", "Workout not found");
        assertEquals("WorkOut/User/user-workouts", view);
    }

    // GET /workout/user/form
    @Test
    void showCreateWorkoutForm_ShouldReturnCreateViewWithNewWorkoutDTO() {
        String view = controller.showCreateWorkoutForm(model);

        verify(model).addAttribute(eq("workoutDTO"), any(WorkoutDTO.class));
        verify(model).addAttribute("user", mockUser);
        assertEquals("WorkOut/User/create-workout", view);
    }

    // POST /workout/user/form
    @Test
    void updateWorkout_ShouldCreateWorkoutAndRedirect() {
        WorkoutDTO workoutDTO = new WorkoutDTO();
        workoutDTO.setTitle("Test");
        workoutDTO.setDescription("desc");
        workoutDTO.setDate(LocalDateTime.now());

        String view = controller.updateWorkout(workoutDTO, model);

        verify(workoutService).createUser_Workouts(any(User_Workouts.class));
        verify(model).addAttribute("message", "Utworzono trening");
        verify(model).addAttribute("user", mockUser);
        assertEquals("redirect:/workout/user", view);
    }


    @Test
    void deleteWorkout_ShouldRedirectWithMessage_WhenSuccess() {
        when(workoutService.deleteById(1L, mockUser.getId())).thenReturn(true);

        String view = controller.deleteWorkout(1L, model);

        verify(model).addAttribute("message", "Workout deleted successfully");
        assertEquals("redirect:/workout/user", view);
    }

    @Test
    void deleteWorkout_ShouldReturnError_WhenFailure() {
        when(workoutService.deleteById(1L, mockUser.getId())).thenReturn(false);

        String view = controller.deleteWorkout(1L, model);

        verify(model).addAttribute("error", "Something went wrong");
        assertEquals("error", view);
    }
}
