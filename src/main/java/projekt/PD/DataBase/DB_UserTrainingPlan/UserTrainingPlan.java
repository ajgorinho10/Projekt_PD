package projekt.PD.DataBase.DB_UserTrainingPlan;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import projekt.PD.DataBase.DB_User.User;

/** Klasa reprezentująca pojedynczy plan treningowy użytkownika*/

@Data
@Entity
@Table(name = "\"usertrainingplan\"")
@AllArgsConstructor
@NoArgsConstructor
public class UserTrainingPlan {

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
    @JoinColumn(name = "users_id")
    @JsonBackReference
    private User user;

    @Nullable
    public Long getId() {
        return id;
    }
}
