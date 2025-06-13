package projekt.PD.Controller.Course_Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseDTO;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.PD.Services.CurrentUser;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;
    private final CurrentUser currentUser;

    public CourseController(CourseService courseService, UserService userService, CurrentUser currentUser) {
        this.courseService = courseService;
        this.currentUser = currentUser;
    }


    /**
     * Obsługuje żądanie GET w celu wyświetlenia wszystkich dostępnych kursów.
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return all-courses
     */
    @GetMapping()
    public String showAllCourses(Model model) {
        List<Course> courses = courseService.findAll();
        List<CourseDTO> courseDTOs = CourseDTO.toDTO(courses);

        model.addAttribute("courses", courseDTOs);
        currentUser.addUserToModel(model);

        return "Course/Users/all-courses";
    }

    /**
     * Zwraca konkretny kurs za pomocą żądania GET w przypadku braku takiego kursu zwraca strone
     * ze kursów do których jest zapisany użytkownik
     *
     * @param id id kursu który chcemy zobaczyc
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return course-details
     */
    @GetMapping("/{id}")
    public String showCourseDetails(@PathVariable Long id, Model model) {
        Optional<Course> course = courseService.findById(id);
        currentUser.addUserToModel(model);

        if (course.isPresent()) {
            CourseDTO courseDTO = new CourseDTO(course.get());
            model.addAttribute("course", courseDTO);

            return "Course/Users/course-details";
        }

        return "redirect:/Course/Users/my-courses";
    }

}
