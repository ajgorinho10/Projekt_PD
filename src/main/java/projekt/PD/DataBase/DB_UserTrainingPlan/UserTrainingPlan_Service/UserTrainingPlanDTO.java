package projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;

import java.util.ArrayList;
import java.util.List;

/** Klasa odpowiada za pobieranie danych planu treningowego użytkownika
 * i mapowaniu ich na obiekt UserTrainigPlan
 * **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTrainingPlanDTO {

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

    public UserTrainingPlanDTO(UserTrainingPlan plan) {
        this.id = plan.getId();
        this.title = plan.getTitle();
        this.description = plan.getDescription();
        this.monday = plan.getMonday();
        this.tuesday = plan.getTuesday();
        this.wednesday = plan.getWednesday();
        this.thursday = plan.getThursday();
        this.friday = plan.getFriday();
        this.saturday = plan.getSaturday();
        this.sunday = plan.getSunday();
    }

    public static List<UserTrainingPlanDTO> toDTO(List<UserTrainingPlan> userTrainingPlans) {
        List<UserTrainingPlanDTO> userTrainingPlansDTOs = new ArrayList<>();
        for (UserTrainingPlan userTrainingPlan : userTrainingPlans) {
            userTrainingPlansDTOs.add(new UserTrainingPlanDTO(userTrainingPlan));
        }

        return userTrainingPlansDTOs;
    }
}
