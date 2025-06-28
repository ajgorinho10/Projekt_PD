package projekt.PD;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import projekt.PD.Controller.Api.CourseRestController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.PD_Course.Course;
import projekt.PD.DataBase.PD_Course.Course_Service.CourseService;
import projekt.PD.Services.CurrentUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseRestController.class)
@Import(CourseRestControllerTest.TestConfig.class)
class CourseRestControllerTest {

    // Konfiguracja mocków dla tego testu (zgodnie z nowymi standardami Springa)
    static class TestConfig {
        @Bean
        public CourseService courseService() {
            return Mockito.mock(CourseService.class);
        }
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
        @Bean
        public CurrentUser currentUser() {
            return Mockito.mock(CurrentUser.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CourseService courseService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CurrentUser currentUser;

    private User testUser;
    private User testTrainerUser;
    private Trainer testTrainer;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        // Użytkownik, który jest studentem
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("test-user");

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

        // Kurs prowadzony przez tego trenera
        testCourse = new Course();
        testCourse.setId(100L);
        testCourse.setTitle("Kurs Jogi");
        testCourse.setUsers(new ArrayList<>());
        testCourse.setCourseTrainer(testTrainer); // Teraz kurs ma trenera, który ma powiązane konto User

    }

    @Test
    @DisplayName("GET /api/course - powinien zwrócić listę wszystkich kursów")
    @WithMockUser
    void shouldGetAllCourses() throws Exception {
        when(courseService.findAll()).thenReturn(List.of(testCourse));

        mockMvc.perform(get("/api/course"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/course/{id} - powinien zwrócić kurs, gdy istnieje")
    @WithMockUser
    void shouldGetCourseById_whenExists() throws Exception {
        // Given
        when(courseService.findById(100L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(get("/api/course/100"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/course/{id} - powinien zwrócić 404, gdy kurs nie istnieje")
    @WithMockUser
    void shouldReturnNotFound_whenCourseDoesNotExist() throws Exception {
        when(courseService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/course/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/course/trainer - trener powinien móc dodać nowy kurs")
    @WithMockUser(username = "trainer-user", roles = "TRAINER")
    void shouldAddCourse_whenUserIsTrainer() throws Exception {
        // Given
        when(userService.findUserByLogin("trainer-user")).thenReturn(testTrainerUser);

        // Tworzymy "surowy" obiekt kursu, który wysyłamy w ciele żądania
        Course newCourse = new Course();
        newCourse.setTitle("Nowy kurs Crossfit");

        // When & Then
        mockMvc.perform(post("/api/course/trainer")
                        .with(csrf()) // Dodajemy token CSRF do żądania POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Course has been added"));

        // Weryfikujemy, czy serwis został wywołany
        verify(courseService).add(any(Course.class));
    }

    @Test
    @DisplayName("DELETE /api/course/trainer/{id} - trener powinien móc usunąć swój kurs")
    @WithMockUser(username = "trainer-user", roles = "TRAINER")
    void shouldDeleteCourse_whenUserIsTrainerAndOwner() throws Exception {
        // Given
        when(userService.findUserByLogin("trainer-user")).thenReturn(testTrainerUser);
        // Symulujemy, że serwis znajduje kurs należący do tego trenera
        when(courseService.findByCourseTrainer_IdAndId(10L, 100L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(delete("/api/course/trainer/100")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Course has been deleted"));

        verify(courseService).deleteById(100L);
    }

    @Test
    @DisplayName("POST /api/course/user/{id} - użytkownik powinien móc dołączyć do kursu")
    @WithMockUser(username = "test-user")
    void shouldAddUserToCourse() throws Exception {
        // Given
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);
        when(courseService.findById(100L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(post("/api/course/user/100").with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().string("User has been added"));

        verify(userService).updateUser(testUser);
    }

    @Test
    @DisplayName("DELETE /api/course/user/{id} - użytkownik powinien móc opuścić kurs")
    @WithMockUser(username = "test-user")
    void shouldDeleteUserFromCourse() throws Exception {
        // Given
        // Musimy najpierw "dodać" użytkownika do kursu, aby metoda removeUser mogła go usunąć
        testCourse.getUsers().add(testUser);
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);
        when(courseService.findById(100L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(delete("/api/course/user/100").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User has been deleted from course"));

        verify(userService).updateUser(testUser);
    }
}