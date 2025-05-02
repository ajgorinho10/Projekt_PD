package projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutDTO {

    private int id;
    private String title;
    private String description;
    private Date date;
    private UserDTO user;
    private TrainerDTO trainer;

    public WorkoutDTO(User_Workouts workout, TrainerDTO trainer,UserDTO user) {
        this.id = workout.getId();
        this.title = workout.getTitle();
        this.description = workout.getDescription();
        this.date = workout.getDate();
        this.user = user;
        this.trainer = trainer;
    }
}
