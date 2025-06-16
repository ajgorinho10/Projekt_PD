package projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

/** Klasa odpowiada za pobieranie danych pojedynczego treningu
 * i mapowaniu ich na obiekt Workout **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime date;
    public WorkoutDTO(User_Workouts workout) {
        this.id = workout.getId();
        this.title = workout.getTitle();
        this.description = workout.getDescription();
        this.date = workout.getDate();
    }

    public static List<WorkoutDTO> toDTO(List<User_Workouts> workouts) {
        List<WorkoutDTO> workoutDTOs = new ArrayList<>();
        for (User_Workouts workout : workouts) {
            workoutDTOs.add(new WorkoutDTO(workout));
        }
        return workoutDTOs;
    }
}
