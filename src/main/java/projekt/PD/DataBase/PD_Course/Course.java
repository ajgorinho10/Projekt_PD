package projekt.PD.DataBase.PD_Course;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"course\"")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "trainers_id",nullable = true)
    private Trainer courseTrainer;

    @ManyToMany(mappedBy = "userCourse")
    private List<User> users = new ArrayList<>();

    @Nullable
    public Long getId() {
        return id;
    }

    @Nullable
    public void setId(Long id) {
        this.id = id;
    }

    public boolean addUser(User user) {
        if(!this.users.contains(user)) {
            this.users.add(user);
            user.getUserCourse().add(this);
            return true;
        }

        return false;
    }

    public boolean removeUser(User user) {
        if(!this.users.contains(user)) {
            return false;
        }
        this.users.remove(user);
        user.getUserCourse().remove(this);
        return true;
    }
}
