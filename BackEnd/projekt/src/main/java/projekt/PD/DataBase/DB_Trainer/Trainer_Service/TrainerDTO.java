package projekt.PD.DataBase.DB_Trainer.Trainer_Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerDTO {
    private int id;
    private UserDTO user;

    public TrainerDTO(UserDTO user) {
        this.id = user.getId();
        this.user = user;
    }
}
