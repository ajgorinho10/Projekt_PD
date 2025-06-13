package projekt.PD.Controller.Course_Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseDTO;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/course/user")
public class UserCourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final CurrentUser currentUser;

    public UserCourseController(CourseService courseService, UserService userService, CurrentUser currentUser) {
        this.courseService = courseService;
        this.userService = userService;
        this.currentUser = currentUser;
    }

    /**
     * Obsługuje metodę GET, Zwraca wszystkie kursy na które zapisany jest użytkownik
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return my-courses
     */
    @GetMapping()
    public String showUserMyCourses(Model model) {
        User user = currentUser.getUserID();
        List<Course> courses = courseService.findByUsers_Id(user.getId());

        List<CourseDTO> dtos = CourseDTO.toDTO(courses);
        model.addAttribute("myCourses", dtos);

        currentUser.addUserToModel(model);
        return "Course/Users/my-courses";
    }

    /**
     * Obsługuje metodę POST w celu dodania użytkownika do kusu
     *
     * @param id id kursu do którego użytkownik ma zostać dodany
     * @param redirectAttributes atrybuty które zostaną dodane do widoku
     * @param request zapytanie HTTP od klienta
     * @return all-courses
     */
    @PostMapping("/{id}")
    public String addUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = currentUser.getUserID();
        Optional<Course> course = courseService.findById(id);

        if(course.isPresent()) {
            if (user.getTrainer() != null && Objects.equals(course.get().getCourseTrainer().getId(), user.getTrainer().getId())) {
                redirectAttributes.addFlashAttribute("message", "This is your course, no need to join.");
            } else if (course.get().addUser(user)) {
                userService.updateUser(user);
                redirectAttributes.addFlashAttribute("message", "User has been added to the course successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Can't add user to course.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Course not found.");
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Obsługuje metode DELETE w celu usunięcia użytkownika z kursu
     *
     * @param id id kursu z którego użytkownik zostanie usunięty
     * @param redirectAttributes atrybuty które zostaną dodane do widoku
     * @param request zapytanie HTTP od klienta
     * @return all-courses
     */
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = currentUser.getUserID();
        Optional<Course> course = courseService.findById(id);
        if(course.isPresent() && course.get().removeUser(user)) {
            //courseService.add(course.get());
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("message", "User has been deleted from course");
        }
        else {
            redirectAttributes.addFlashAttribute("error", "Can't delete user from course");
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

}
