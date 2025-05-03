package projekt.PD.DataBase.DB_Trainer.Trainer_Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerDTO {
    private Long id;
    private String specialization;
    private String firstName;
    private String lastName;

    public TrainerDTO(User user) {
        this.id = user.getId();
        this.specialization = user.getTrainer().getSpecialization();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }
}
