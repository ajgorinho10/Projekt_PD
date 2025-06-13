package projekt.PD.DataBase.PD_Course.Course_Service;

import projekt.PD.DataBase.PD_Course.Course;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    Optional<Course> findById(Long id);
    Optional<Course> findByCourseTrainer_IdAndId(Long courseId, Long trainerId);
    Optional<Course> findByIdAndUsers_Id(Long id, Long userId);

    List<Course> findByCourseTrainer_Id(Long id);
    List<Course> findByUsers_Id(Long usersId);
    List<Course> findAll();

    boolean deleteById(Long id);
    boolean add(Course course);
}
