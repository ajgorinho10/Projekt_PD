package projekt.PD.DataBase.PD_Course;

import org.springframework.data.domain.Limit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/** Interfejs związanych z tabelą users zapytań do bazy danych */

public interface CourseRepository extends CrudRepository<Course, Long> {
    Optional<Course> findById(Long id);
    Optional<Course> findByCourseTrainer_IdAndId(Long courseId, Long trainerId);
    Optional<Course> findByIdAndUsers_Id(Long id, Long userId);
    List<Course> findByCourseTrainer_Id(Long id);
    List<Course> findByUsers_Id(Long usersId);
}
