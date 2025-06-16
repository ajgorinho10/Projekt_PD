package projekt.PD.DataBase.DB_TrainerPlan;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.PD_Course.Course;

import java.util.ArrayList;
import java.util.List;
/** Klasa reprezentująca pojedynczy plan treningowy założony przez trenera
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "\"trainerplan\"")
public class TrainerPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    private String description;

    @Nullable
    private String monday;
    @Nullable
    private String tuesday;
    @Nullable
    private String wednesday;
    @Nullable
    private String thursday;
    @Nullable
    private String friday;
    @Nullable
    private String saturday;
    @Nullable
    private String sunday;

    @ManyToOne
    @JoinColumn(name = "trainers_id",nullable = true)
    private Trainer planTrainer;

    @ManyToOne
    @JoinTable(
            name = "user_trainingplan_bytrainer",
            joinColumns = @JoinColumn(name = "trainerplan_id"),
            inverseJoinColumns = @JoinColumn(name = "users_id")
    )
    private User trainerPlanUser;

    public TrainerPlan (TrainerPlanDTO trainerPlanDTO) {
        this.title = trainerPlanDTO.getTitle();
        this.description = trainerPlanDTO.getDescription();
        this.monday = trainerPlanDTO.getMonday();
        this.tuesday = trainerPlanDTO.getTuesday();
        this.wednesday = trainerPlanDTO.getWednesday();
        this.thursday = trainerPlanDTO.getThursday();
        this.friday = trainerPlanDTO.getFriday();
        this.saturday = trainerPlanDTO.getSaturday();
        this.sunday = trainerPlanDTO.getSunday();
    }

}
