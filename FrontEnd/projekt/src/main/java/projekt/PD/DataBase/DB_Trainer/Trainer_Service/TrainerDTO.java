package projekt.PD.DataBase.DB_Trainer.Trainer_Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;

import java.util.ArrayList;
import java.util.List;

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
        assert user.getTrainer() != null;
        this.specialization = user.getTrainer().getSpecialization();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    public static List<TrainerDTO> toDTO(List<Trainer> trainers) {
        List<TrainerDTO> trainerDTO = new ArrayList<>();
        for (Trainer trainer : trainers) {
            trainerDTO.add(new TrainerDTO(trainer.getUser()));
        }
        return trainerDTO;
    }
}
