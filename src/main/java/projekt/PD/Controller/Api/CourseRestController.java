package projekt.PD.Controller.Api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseDTO;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Kontroler Rest API do obsługi żądań związanych z zarządzaniem kursami */

@RestController
@RequestMapping("/api/course")
public class CourseRestController {

    private final CourseService courseService;
    private final UserService userService;

    public CourseRestController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    // Pobiera Wszystkie kursy w bazie
    @GetMapping
    public ResponseEntity<?> getCourses() {
        List<Course> courses = courseService.findAll();
        List<CourseDTO> courseDTO = CourseDTO.toDTO(courses);

        return new ResponseEntity<>(courseDTO, HttpStatus.OK);
    }

    //Pobiera kurs po id
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseService.findById(id);
        if (course.isPresent()) {
            CourseDTO courseDTO = new CourseDTO(course.get());
            return new ResponseEntity<>(courseDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //Pobiera wszystkie kursy Trenera
    @GetMapping("/trainer")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<?> getCourseTrainer() {
        User user = getUserID();
        List<Course> courses = courseService.findByCourseTrainer_Id(user.getTrainer().getId());
        if(!courses.isEmpty()) {
            return new ResponseEntity<>(CourseDTO.toDTO(courses), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //Dodaje kurs jako trener
    @PostMapping("/trainer")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<?> addCourse(@RequestBody Course course) {
        User user = getUserID();
        course.setId(null);
        assert user.getTrainer() != null;
        course.setCourseTrainer(user.getTrainer());
        course.setUsers(new ArrayList<>());
        courseService.add(course);

        return new ResponseEntity<>("Course has been added", HttpStatus.CREATED);
    }

    //Usuwa kurs jako trener
    @DeleteMapping("/trainer/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        User user = getUserID();
        Optional<Course> course = courseService.findByCourseTrainer_IdAndId(user.getTrainer().getId(), id);
        if(course.isPresent()) {
            courseService.deleteById(course.get().getId());
            return new ResponseEntity<>("Course has been deleted",HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Course not found",HttpStatus.NOT_FOUND);
        }

    }

    //Pobiera wszystkie kursy na które zapisany jest urzytkownik
    @GetMapping("/user")
    public ResponseEntity<?> getCourseUsers() {
        User user = getUserID();
        List<Course> courses = courseService.findByUsers_Id(user.getId());
        if(!courses.isEmpty()) {
            return new ResponseEntity<>(CourseDTO.toDTO(courses), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //Dodaje urzytkownika do kursu
    @PostMapping("/user/{id}")
    public ResponseEntity<?> addUser(@PathVariable Long id) {
        User user = getUserID();
        Optional<Course> course = courseService.findById(id);

        if(course.isPresent()) {
            if(user.getTrainer() != null && Objects.equals(course.get().getCourseTrainer().getId(), user.getTrainer().getId())) {
                return new ResponseEntity<>("This is your course",HttpStatus.OK);
            }

                if(course.get().addUser(user)) {
                    //courseService.add(course.get());
                    userService.updateUser(user);
                    return new ResponseEntity<>("User has been added", HttpStatus.CREATED);
                }
        }

        return new ResponseEntity<>("Can't add user to course",HttpStatus.NOT_FOUND);
    }

    //Usuwa urzytkownika z kursu
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = getUserID();
        Optional<Course> course = courseService.findById(id);
        if(course.isPresent() && course.get().removeUser(user)) {
            //courseService.add(course.get());
            userService.updateUser(user);
            return new ResponseEntity<>("User has been deleted from course",HttpStatus.OK);
        }

        return new ResponseEntity<>("Can't delete user from course",HttpStatus.NOT_FOUND);
    }

    //Funkcja pomocnicza do indentyfikowania uzytkownika
    private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }
}
