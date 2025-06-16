package projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;

import java.util.ArrayList;
import java.util.List;

/** Klasa odpowiada za pobieranie danych planu treningowego od trenera
 * i mapowaniu ich na obiekt TrainerPlan
 * **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerPlanDTO {

    private Long id;
    private String title;
    private String description;
    private String monday;
    private String tuesday;
    private String wednesday;
    private String thursday;
    private String friday;
    private String saturday;
    private String sunday;

    private UserDTO user;
    private TrainerDTO trainer;

    public TrainerPlanDTO(TrainerPlan trainerPlan) {
        this.id = trainerPlan.getId();
        this.title = trainerPlan.getTitle();
        this.description = trainerPlan.getDescription();
        this.monday = trainerPlan.getMonday();
        this.tuesday = trainerPlan.getTuesday();
        this.wednesday = trainerPlan.getWednesday();
        this.thursday = trainerPlan.getThursday();
        this.friday = trainerPlan.getFriday();
        this.saturday = trainerPlan.getSaturday();
        this.sunday = trainerPlan.getSunday();
        this.user = new UserDTO(trainerPlan.getTrainerPlanUser());
        this.trainer = new TrainerDTO(trainerPlan.getPlanTrainer().getUser());
    }

    public static List<TrainerPlanDTO> toDTO(List<TrainerPlan> trainerPlans) {
        List<TrainerPlanDTO> trainerPlansDTO = new ArrayList<>();
        for (TrainerPlan trainerPlan : trainerPlans) {
            trainerPlansDTO.add(new TrainerPlanDTO(trainerPlan));
        }

        return trainerPlansDTO;
    }
}
