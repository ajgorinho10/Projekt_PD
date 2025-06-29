package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import projekt.PD.Controller.Course_Controller.CourseController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseDTO;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;
import projekt.PD.Services.CurrentUser;
import projekt.PD.DataBase.DB_User.User_Service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseControllerUnitTest {

    private CourseController courseController;
    private CourseService courseService;
    private CurrentUser currentUser;

    private User testUser;
    private Trainer testTrainer;
    private User testTrainerUser;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        currentUser = mock(CurrentUser.class);
        UserService userService = mock(UserService.class); // jeśli kontroler go przyjmuje
        courseController = new CourseController(courseService, userService, currentUser);

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("test-user");
        testUser.setRoles("ROLE_USER");
        testUser.setTrainer(null);

        // Trener (jako encja Trainer)
        testTrainer = new Trainer();
        testTrainer.setId(10L);

        // Użytkownik, który jest trenerem (konto użytkownika dla trenera)
        testTrainerUser = new User();
        testTrainerUser.setId(2L);
        testTrainerUser.setLogin("trainer-user");
        testTrainerUser.setTrainer(testTrainer); // Powiązanie konta User z profilem Trainer

        // DODAJ TĘ LINIĘ, ABY POWIĄZAĆ TRENERA Z JEGO KONTEM UŻYTKOWNIKA
        testTrainer.setUser(testTrainerUser);
        when(userService.findUserByLogin(testTrainerUser.getLogin())).thenReturn(testTrainerUser);

        testCourse = new Course();
        testCourse.setId(100L);
        testCourse.setTitle("Test Course");
        testCourse.setUsers(new ArrayList<>());
        testCourse.setCourseTrainer(testTrainer); // Teraz kurs ma trenera, który ma powiązane konto User
    }

    @Test
    void showAllCourses_ShouldAddCoursesToModel_AndReturnAllCoursesView() {
        // Test sprawdza, czy metoda showAllCourses poprawnie:
        // - dodaje listę kursów do modelu
        // - zwraca właściwą nazwę widoku
        // - wykorzystuje serwis CurrentUser do dodania użytkownika do modelu

        // given
        when(courseService.findAll()).thenReturn(List.of(testCourse));
        Model model = new ConcurrentModel();

        // when
        String viewName = courseController.showAllCourses(model);

        // then
        assertEquals("Course/Users/all-courses", viewName); // poprawna ścieżka widoku
        assertTrue(model.containsAttribute("courses")); // kursy zostały dodane do modelu
        List<CourseDTO> dtos = (List<CourseDTO>) model.getAttribute("courses");
        assertEquals(1, dtos.size());
        assertEquals("Test Course", dtos.get(0).getTitle());
        verify(currentUser).addUserToModel(model); // użytkownik został dodany do modelu
    }

    @Test
    void showCourseDetails_ShouldReturnDetailsView_WhenCourseExists() {
        // Test sprawdza, czy metoda showCourseDetails:
        // - poprawnie znajduje istniejący kurs po ID
        // - dodaje kurs do modelu jako CourseDTO
        // - zwraca widok szczegółów kursu
        // - dodaje użytkownika do modelu

        // given
        when(courseService.findById(testCourse.getId())).thenReturn(Optional.of(testCourse));
        Model model = new ConcurrentModel();

        // when
        String viewName = courseController.showCourseDetails(testCourse.getId(), model);

        // then
        assertEquals("Course/Users/course-details", viewName); // widok szczegółów
        assertTrue(model.containsAttribute("course")); // kurs dodany do modelu
        CourseDTO dto = (CourseDTO) model.getAttribute("course");
        assertEquals("Test Course", dto.getTitle()); // poprawne dane kursu
        verify(currentUser).addUserToModel(model); // użytkownik dodany do modelu
    }

    @Test
    void showCourseDetails_ShouldRedirect_WhenCourseDoesNotExist() {
        // Test sprawdza, czy metoda showCourseDetails:
        // - przekierowuje użytkownika na listę kursów, jeśli kurs o podanym ID nie istnieje
        // - mimo wszystko dodaje użytkownika do modelu

        // given
        Long courseId = 999L;
        when(courseService.findById(courseId)).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        // when
        String viewName = courseController.showCourseDetails(courseId, model);

        // then
        assertEquals("redirect:/Course/Users/my-courses", viewName); // przekierowanie
        verify(currentUser).addUserToModel(model); // użytkownik dodany do modelu mimo braku kursu
    }
}
