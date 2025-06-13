package projekt.PD.DataBase.PD_Course.Course_Service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerDTO;
import projekt.PD.DataBase.PD_Course.Course;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {

    private Long id;
    private String title;
    private TrainerDTO courseTrainer;
    private int countUsers;

    public CourseDTO(Course course){
        this.id = course.getId();
        this.title = course.getTitle();
        this.countUsers = course.getUsers().size();
        this.courseTrainer = new TrainerDTO(course.getCourseTrainer().getUser());
    }

    public static List<CourseDTO> toDTO(List<Course> courses){
        List<CourseDTO> courseDTO = new ArrayList<>();
        for(Course course : courses){
            courseDTO.add(new CourseDTO(course));
        }
        return courseDTO;
    }
}
