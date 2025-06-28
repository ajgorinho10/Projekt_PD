package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.CourseRepository;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {

        course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Kurs Jogi");

        course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Kurs CrossFit");
    }

    @Test
    @DisplayName("Powinien zwrócić kurs, gdy zostanie znaleziony po ID")
    void shouldReturnCourseWhenFoundById() {

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));

        Optional<Course> result = courseService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(course1);
        verify(courseRepository).findById(1L); // Weryfikujemy, czy metoda na repozytorium została wywołana
    }

    @Test
    @DisplayName("Powinien zwrócić pusty Optional, gdy kurs o danym ID nie istnieje")
    void shouldReturnEmptyOptionalWhenCourseNotFoundById() {

        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Course> result = courseService.findById(99L);

        assertThat(result).isNotPresent();
        verify(courseRepository).findById(99L);
    }

    @Test
    @DisplayName("Powinien zwrócić listę wszystkich kursów")
    void shouldReturnAllCourses() {

        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));

        List<Course> result = courseService.findAll();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(course1, course2);
        verify(courseRepository).findAll();
    }

    @Test
    @DisplayName("Powinien wywołać metodę save na repozytorium podczas dodawania kursu")
    void shouldCallSaveWhenAddingCourse() {

        Course newCourse = new Course();
        newCourse.setTitle("Nowy Kurs");

        boolean result = courseService.add(newCourse);

        assertThat(result).isTrue();

        verify(courseRepository).save(newCourse);
    }

    @Test
    @DisplayName("Powinien zwrócić listę kursów dla danego ID trenera")
    void shouldReturnCoursesForGivenTrainerId() {

        Long trainerId = 5L;
        when(courseRepository.findByCourseTrainer_Id(trainerId)).thenReturn(List.of(course1));

        List<Course> result = courseService.findByCourseTrainer_Id(trainerId);

        assertThat(result).isNotNull();
        assertThat(result).containsExactly(course1);
        verify(courseRepository).findByCourseTrainer_Id(trainerId);
    }

    @Test
    @DisplayName("Powinien zwrócić listę kursów dla danego ID użytkownika")
    void shouldReturnCoursesForGivenUsersId() {

        Long userId = 7L;
        when(courseRepository.findByUsers_Id(userId)).thenReturn(List.of(course2));

        List<Course> result = courseService.findByUsers_Id(userId);

        assertThat(result).isNotNull();
    }
}