package projekt.PD.DataBase.DB_Trainer;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.PD_Course.Course;

import java.util.List;


@Data
@Entity
@Table(name = "\"trainers\"")
@AllArgsConstructor
@NoArgsConstructor
public class Trainer {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference
    private User user;

    @Column(nullable = false)
    private String specialization;

    @OneToMany(mappedBy = "courseTrainer")
    private List<Course> courses;

    @Nullable
    public Long getId() {
        return this.id;
    }

}
