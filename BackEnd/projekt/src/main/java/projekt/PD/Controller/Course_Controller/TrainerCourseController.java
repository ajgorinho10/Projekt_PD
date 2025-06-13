package projekt.PD.Controller.Course_Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseDTO;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/course/trainer")
public class TrainerCourseController {

    private final CurrentUser currentUser;
    private final CourseService courseService;

    public TrainerCourseController(CurrentUser currentUser, UserService userService, CourseService courseService) {
        this.currentUser = currentUser;
        this.courseService = courseService;
    }

    /**
     * Obsługuje żdanie GET dodaje do modelu wszystkie kursy trenera
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return trainer-courses
     */
    @GetMapping()
    @PreAuthorize("hasRole('TRAINER')")
    public String showTrainerCourses(Model model) {
        User user = currentUser.getUserID();
        if(user.getTrainer()==null){
            return "redirect:/Course/Users/my-courses";
        }

        List<Course> courses = courseService.findByCourseTrainer_Id(user.getTrainer().getId());
        List<CourseDTO> courseDTOS = CourseDTO.toDTO(courses);

        model.addAttribute("trainerCourses", courseDTOS);
        currentUser.addUserToModel(model);

        return "Course/Trainer/trainer-courses";
    }

    /**
     *  Obsługuje żądanie GET, dodaje do modelu nową klase Curse w celu stworzenia nowego kursu
     *
     * @param model obiekt Springa służący do przekazywania danych do widoku
     * @return add-course
     */
    @GetMapping("/form")
    @PreAuthorize("hasRole('TRAINER')")
    public String showAddCourseForm(Model model) {
        model.addAttribute("course", new Course());
        currentUser.addUserToModel(model);

        return "Course/Trainer/add-course";
    }

    /**
     * Obsługuje metodę POST w celu dodania kursu do bazy
     *
     * @param course klasa course która ma zostać dodana do bazy
     * @param bindingResult sprawdzenie błędów
     * @return trainer
     */
    @PostMapping("/form")
    @PreAuthorize("hasRole('TRAINER')")
    public String submitCourseForm(@ModelAttribute("course") @Valid Course course, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "Course/Trainer/add-course";
        }

        User user = currentUser.getUserID();
        course.setId(null);
        course.setCourseTrainer(user.getTrainer());
        courseService.add(course);

        return "redirect:/course/trainer";
    }

    /**
     * Obsługuje żądanie DELETE w celu usunięcia kursu przez trenera
     *
     * @param id id kursu który trener chce usunąć
     * @param redirectAttributes atrybuty które zostaną dodane do widoku
     * @param request zapytanie HTTP od klienta
     * @return trainer-courses
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = currentUser.getUserID();
        assert user.getTrainer() != null;

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
}
