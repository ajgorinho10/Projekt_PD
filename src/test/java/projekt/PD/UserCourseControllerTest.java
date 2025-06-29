package projekt.PD;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.PD.Controller.Course_Controller.UserCourseController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;
import projekt.PD.Services.CurrentUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserCourseControllerTest {

    private CourseService courseService;
    private UserService userService;
    private CurrentUser currentUser;
    private UserCourseController controller;

    private User user;
    private Course course;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        userService = mock(UserService.class);
        currentUser = mock(CurrentUser.class);
        controller = new UserCourseController(courseService, userService, currentUser);

        user = new User();
        user.setId(1L);

        trainer = new Trainer();
        trainer.setId(10L);


        course = spy(new Course());
        course.setId(100L);
        course.setTitle("Test Course");
        course.setUsers(new ArrayList<>());
        course.setCourseTrainer(trainer);
    }

    // === showUserMyCourses() ===

    @Test
    void showUserMyCourses_ShouldAddCoursesToModelAndReturnView() {
        // given
        trainer.setUser(user);
        user.setTrainer(trainer);
        when(currentUser.getUserID()).thenReturn(user);
        when(courseService.findByUsers_Id(user.getId())).thenReturn(List.of(course));
        Model model = new ConcurrentModel();

        // when
        String view = controller.showUserMyCourses(model);

        // then
        assertEquals("Course/Users/my-courses", view);
        assertTrue(model.containsAttribute("myCourses"));
        verify(currentUser).addUserToModel(model);
    }

    // === addUser() ===

    @Test
    void addUser_ShouldAddUserToCourse_WhenNotTrainerAndNotAlreadyAdded() {
        // given
        when(currentUser.getUserID()).thenReturn(user);
        when(courseService.findById(course.getId())).thenReturn(Optional.of(course));
        when(course.addUser(user)).thenReturn(true);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn("/referer");
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // when
        String view = controller.addUser(course.getId(), redirectAttributes, request);

        // then
        assertEquals("redirect:/referer", view);
        verify(userService).updateUser(user);
        verify(redirectAttributes).addFlashAttribute("message", "User has been added to the course successfully!");
    }

    @Test
    void addUser_ShouldNotAdd_WhenUserIsTrainerOfThatCourse() {
        // given
        user.setTrainer(trainer);
        when(currentUser.getUserID()).thenReturn(user);
        when(courseService.findById(course.getId())).thenReturn(Optional.of(course));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn("/referer");
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // when
        String view = controller.addUser(course.getId(), redirectAttributes, request);

        // then
        assertEquals("redirect:/referer", view);
        verify(redirectAttributes).addFlashAttribute("message", "This is your course, no need to join.");
        verify(userService, never()).updateUser(any());
    }

    @Test
    void addUser_ShouldShowError_WhenUserAlreadyInCourse() {
        // given
        when(currentUser.getUserID()).thenReturn(user);
        when(courseService.findById(course.getId())).thenReturn(Optional.of(course));
        when(course.addUser(user)).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn("/ref");
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // when
        String view = controller.addUser(course.getId(), redirectAttributes, request);

        // then
        assertEquals("redirect:/ref", view);
        verify(redirectAttributes).addFlashAttribute("error", "Can't add user to course.");
    }

    @Test
    void addUser_ShouldShowError_WhenCourseNotFound() {
        // given
        when(currentUser.getUserID()).thenReturn(user);
        when(courseService.findById(999L)).thenReturn(Optional.empty());

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn(null); // no referer
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // when
        String view = controller.addUser(999L, redirectAttributes, request);

        // then
        assertEquals("redirect:/", view);
        verify(redirectAttributes).addFlashAttribute("error", "Course not found.");
    }

    // === deleteUser() ===

    @Test
    void deleteUser_ShouldRemoveUserFromCourse_WhenSuccess() {
        // given
        when(currentUser.getUserID()).thenReturn(user);
        when(courseService.findById(course.getId())).thenReturn(Optional.of(course));
        when(course.removeUser(user)).thenReturn(true);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn("/prev");
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // when
        String view = controller.deleteUser(course.getId(), redirectAttributes, request);

        // then
        assertEquals("redirect:/prev", view);
        verify(userService).updateUser(user);
        verify(redirectAttributes).addFlashAttribute("message", "User has been deleted from course");
    }

    @Test
    void deleteUser_ShouldShowError_WhenRemoveFails() {
        // given
        when(currentUser.getUserID()).thenReturn(user);
        when(courseService.findById(course.getId())).thenReturn(Optional.of(course));
        when(course.removeUser(user)).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn(null);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // when
        String view = controller.deleteUser(course.getId(), redirectAttributes, request);

        // then
        assertEquals("redirect:/", view);
        verify(redirectAttributes).addFlashAttribute("error", "Can't delete user from course");
        verify(userService, never()).updateUser(any());
    }

    @Test
    void deleteUser_ShouldShowError_WhenCourseNotFound() {
        // given
        when(currentUser.getUserID()).thenReturn(user);
        when(courseService.findById(999L)).thenReturn(Optional.empty());

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn("/back");
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // when
        String view = controller.deleteUser(999L, redirectAttributes, request);

        // then
        assertEquals("redirect:/back", view);
        verify(redirectAttributes).addFlashAttribute("error", "Can't delete user from course");
    }
}
