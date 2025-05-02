package projekt.PD.DataBase.DB_UserWorkout;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;

import java.util.Date;

@Data
@Entity
@Table(name = "\"user_workout\"")
public class User_Workouts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    private String title;
    private String description;
    private Date date;

    @ManyToOne
    @JoinColumn(name = "trainers_id")
    @JsonBackReference
    private Trainer trainer;

    @ManyToOne
    @JoinColumn(name = "users_id")
    @JsonManagedReference
    private User user;

}
