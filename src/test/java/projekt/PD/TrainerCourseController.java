package projekt.PD;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.PD.Controller.Course_Controller.TrainerCourseController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;
import projekt.PD.Services.CurrentUser;
import projekt.PD.DataBase.DB_User.User_Service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerCourseControllerUnitTest {

    private TrainerCourseController controller;
    private CourseService courseService;
    private CurrentUser currentUser;

    private User trainerUser;
    private Trainer trainer;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        currentUser = mock(CurrentUser.class);
        UserService userService = mock(UserService.class);
        controller = new TrainerCourseController(currentUser, userService, courseService);

        trainer = new Trainer();
        trainer.setId(1L);

        trainerUser = new User();
        trainerUser.setId(10L);
        trainerUser.setLogin("trainer1");
        trainerUser.setTrainer(trainer);
        trainer.setUser(trainerUser);

        testCourse = new Course();
        testCourse.setId(100L);
        testCourse.setTitle("Java Basics");
        testCourse.setCourseTrainer(trainer);
    }

    @Test
    void showTrainerCourses_ShouldAddCoursesToModel_AndReturnView() {
        // Test sprawdza czy trener widzi swoje kursy

        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(courseService.findByCourseTrainer_Id(trainer.getId())).thenReturn(List.of(testCourse));
        Model model = new ConcurrentModel();

        String viewName = controller.showTrainerCourses(model);

        assertEquals("Course/Trainer/trainer-courses", viewName);
        assertTrue(model.containsAttribute("trainerCourses"));
        verify(currentUser).addUserToModel(model);
    }

    @Test
    void showTrainerCourses_ShouldRedirect_WhenUserIsNotTrainer() {
        // Test sprawdza czy użytkownik bez przypisanego trenera zostaje przekierowany

        User userWithoutTrainer = new User();
        userWithoutTrainer.setId(99L);
        userWithoutTrainer.setTrainer(null);

        when(currentUser.getUserID()).thenReturn(userWithoutTrainer);
        Model model = new ConcurrentModel();

        String viewName = controller.showTrainerCourses(model);

        assertEquals("redirect:/Course/Users/my-courses", viewName);
    }

    @Test
    void showAddCourseForm_ShouldReturnAddCourseViewAndAttachEmptyCourse() {
        // Test sprawdza, czy formularz dodawania kursu jest wyświetlany z pustym obiektem Course

        Model model = new ConcurrentModel();

        String viewName = controller.showAddCourseForm(model);

        assertEquals("Course/Trainer/add-course", viewName);
        assertTrue(model.containsAttribute("course"));
        verify(currentUser).addUserToModel(model);
    }

    @Test
    void submitCourseForm_ShouldAddCourseAndRedirect_WhenNoErrors() {
        // Test sprawdza czy poprawnie wypełniony formularz dodaje kurs i przekierowuje

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(currentUser.getUserID()).thenReturn(trainerUser);

        Course course = new Course();
        course.setTitle("New Course");

        String viewName = controller.submitCourseForm(course, bindingResult);

        assertEquals("redirect:/course/trainer", viewName);
        assertEquals(trainer, course.getCourseTrainer());
        verify(courseService).add(course);
    }

    @Test
    void submitCourseForm_ShouldReturnFormView_WhenErrorsExist() {
        // Test sprawdza czy formularz z błędami zwraca widok bez zapisu

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        Course course = new Course();
        String viewName = controller.submitCourseForm(course, bindingResult);

        assertEquals("Course/Trainer/add-course", viewName);
        verify(courseService, never()).add(any());
    }

    @Test
    void deleteCourse_ShouldDeleteCourseAndRedirectToReferer_WhenCourseExists() {
        // Test sprawdza czy trener może usunąć swój kurs i czy jest przekierowany do referera

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(courseService.findByCourseTrainer_IdAndId(trainer.getId(), testCourse.getId()))
                .thenReturn(Optional.of(testCourse));
        when(request.getHeader("Referer")).thenReturn("/previous/page");

        String result = controller.deleteCourse(testCourse.getId(), redirectAttributes, request);

        assertEquals("redirect:/previous/page", result);
        verify(courseService).deleteById(testCourse.getId());
        verify(redirectAttributes).addFlashAttribute("message", "Course has been deleted");
    }

    @Test
    void deleteCourse_ShouldAddErrorMessage_WhenCourseNotFound() {
        // Test sprawdza czy dodawany jest komunikat błędu, jeśli kurs nie istnieje

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(courseService.findByCourseTrainer_IdAndId(trainer.getId(), 999L))
                .thenReturn(Optional.empty());
        when(request.getHeader("Referer")).thenReturn(null); // brak referera

        String result = controller.deleteCourse(999L, redirectAttributes, request);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Dafuq");
        verify(courseService, never()).deleteById(any());
    }
}
