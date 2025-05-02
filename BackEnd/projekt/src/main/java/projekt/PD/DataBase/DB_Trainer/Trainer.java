package projekt.PD.DataBase.DB_Trainer;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "\"trainers\"")
@AllArgsConstructor
@NoArgsConstructor
public class Trainer {

    @Id
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference
    private User user;

    @Column(nullable = false)
    private String specialization;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<User_Workouts> createdWorkouts = new ArrayList<>();

}
