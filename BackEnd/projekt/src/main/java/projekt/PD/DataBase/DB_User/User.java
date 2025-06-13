package projekt.PD.DataBase.DB_User;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.lang.Nullable;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;
import projekt.PD.DataBase.PD_Course.Course;

import java.util.*;

@Entity
@Table(name = "\"users\"")
@Data
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String roles;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Trainer trainer;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<User_Workouts> user_workouts = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<UserTrainingPlan> userTrainingPlans = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_course",
            joinColumns = @JoinColumn(name = "users_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> userCourse = new ArrayList<>();

    @OneToMany(mappedBy = "trainerPlanUser")
    private List<TrainerPlan> trainerPlan = new ArrayList<>();

    @Nullable
    public Trainer getTrainer() {
        return this.trainer;
    }

    public boolean isUserExistInCourse(Long courseId) {
        for(Course course: userCourse){
            if(course.getId()!=null && course.getId().equals(courseId)){
                return true;
            }
        }

        return false;
    }
}
