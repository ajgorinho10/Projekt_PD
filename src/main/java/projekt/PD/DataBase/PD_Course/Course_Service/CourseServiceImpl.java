package projekt.PD.DataBase.PD_Course.Course_Service;

import org.springframework.stereotype.Service;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.CourseRepository;

import java.util.List;
import java.util.Optional;

/** Implementacja metod związanych z zarządzaniem kursami */

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public Optional<Course> findByCourseTrainer_IdAndId(Long courseId, Long trainerId) {
        return courseRepository.findByCourseTrainer_IdAndId(courseId, trainerId);
    }

    @Override
    public Optional<Course> findByIdAndUsers_Id(Long id, Long userId) {
        return courseRepository.findByIdAndUsers_Id(id, userId);
    }

    @Override
    public List<Course> findByCourseTrainer_Id(Long id) {
        return courseRepository.findByCourseTrainer_Id(id);
    }

    @Override
    public List<Course> findByUsers_Id(Long usersId) {
        return courseRepository.findByUsers_Id(usersId);
    }

    @Override
    public List<Course> findAll() {
        return (List<Course>) courseRepository.findAll();
    }

    @Override
    public boolean deleteById(Long id) {
        courseRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean add(Course course) {
        courseRepository.save(course);
        return true;
    }
}
