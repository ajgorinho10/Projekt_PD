package projekt.PD.DataBase.DB_UserWorkout;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.Nullable;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;


import java.time.LocalDateTime;

/** Klasa reprezentująca pojedynczy trening */

@Data
@Entity
@Table(name = "\"user_workout\"")
@AllArgsConstructor
@NoArgsConstructor
public class User_Workouts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "users_id")
    @JsonBackReference
    private User user;

    @Nullable
    public Long getId(){
        return id;
    }
}
