package projekt.PD.Controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseDTO;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    public CourseController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }


    //Pobiera wszystkie kursy w bazie i wyswietla je na stronie
    @GetMapping("/all")
    public String showAllCourses(Model model) {
        List<Course> courses = courseService.findAll();
        List<CourseDTO> courseDTOs = CourseDTO.toDTO(courses);
        model.addAttribute("courses", courseDTOs);
        return "all-courses"; // Thymeleaf template
    }

    //Pobiera kurs po id

    
    @GetMapping("/{id}")
    public String showCourseDetails(@PathVariable Long id, Model model) {
        Optional<Course> course = courseService.findById(id);
        if (course.isPresent()) {
            CourseDTO courseDTO = new CourseDTO(course.get());
            model.addAttribute("course", courseDTO);
            return "course-details"; // Thymeleaf template
        }
        return "redirect:/course"; // Redirect if course not found
    }


    //Pobiera wszystkie kursy Trenera
@GetMapping("/trainer")
@PreAuthorize("hasRole('TRAINER')")
public String showTrainerCourses(Model model) {
    User user = getUserID();
    List<Course> courses = courseService.findByCourseTrainer_Id(user.getTrainer().getId());
    List<CourseDTO> dtos = CourseDTO.toDTO(courses);
    model.addAttribute("trainerCourses", dtos);
    return "trainer-courses";
}




    // Show the form
    @GetMapping("/form")
    @PreAuthorize("hasRole('TRAINER')")
    public String showAddCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "add-course"; // Thymeleaf template
    }

    // Handle form submission
    @PostMapping("/form")
    @PreAuthorize("hasRole('TRAINER')")
    public String submitCourseForm(@ModelAttribute("course") @Valid Course course, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "add-course";
        }

        User user = getUserID();
        course.setId(null);
        course.setCourseTrainer(user.getTrainer());
        courseService.add(course);

        return "redirect:/course/trainer";
    }



    //Usuwa kurs jako trener
    @DeleteMapping("/trainer/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = getUserID();
        Optional<Course> course = courseService.findByCourseTrainer_IdAndId(user.getTrainer().getId(), id);
        if(course.isPresent()) {
            courseService.deleteById(course.get().getId());
            redirectAttributes.addFlashAttribute("message", "Course has been deleted");
        }
        else{
            redirectAttributes.addFlashAttribute("error", "Dafuq");
        }
            // Redirect back to the referring page or to a default page if no referrer
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");

    }

    //Pobiera wszystkie kursy na które zapisany jest uzytkownik
    @GetMapping("/user")
    public String showUserMyCourses(Model model) {
        User user = getUserID();
        List<Course> courses = courseService.findByUsers_Id(user.getId());
        List<CourseDTO> dtos = CourseDTO.toDTO(courses);
        model.addAttribute("myCourses", dtos);
        return "my-courses"; // Thymeleaf template
    }




    //Dodaje uzytkownika do kursu
@PostMapping("/user/{id}")
public String addUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    User user = getUserID();
    Optional<Course> course = courseService.findById(id);

    if (course.isPresent()) {
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

    // Redirect back to the referring page or to a default page if no referrer
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/");
    }


    //Usuwa uzytkownika z kursu
    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = getUserID();
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

    //Funkcja pomocnicza do indentyfikowania uzytkownika
    private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }
}
