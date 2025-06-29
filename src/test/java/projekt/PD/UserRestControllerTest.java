package projekt.PD;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import projekt.PD.Controller.Api.UserRestController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutService;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
@Import(UserRestControllerTest.TestConfig.class)
class UserRestControllerTest {

    // Konfiguracja mocków dla wszystkich czterech serwisów
    static class TestConfig {
        @Bean public UserService userService() { return Mockito.mock(UserService.class); }
        @Bean public User_WorkoutService userWorkoutService() { return Mockito.mock(User_WorkoutService.class); }
        @Bean public TrainerService trainerService() { return Mockito.mock(TrainerService.class); }
        @Bean public UserTrainingPlanService userTrainingPlanService() { return Mockito.mock(UserTrainingPlanService.class); }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private UserService userService;
    @Autowired private User_WorkoutService userWorkoutService;
    @Autowired private TrainerService trainerService;
    @Autowired private ObjectMapper objectMapper;

    private User testUser;
    private User testTrainer;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("test-user");
        testUser.setRoles("ROLE_USER");

        testTrainer = new User();
        testTrainer.setId(2L);
        testTrainer.setLogin("test-trainer");
        testTrainer.setRoles("ROLE_TRAINER");
        testTrainer.setTrainer(new Trainer(2L,testTrainer,"Specjalizacja 1",List.of(),List.of()));
    }

    @Test
    @DisplayName("GET /api/users - powinien zezwolić na dostęp dla admina")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldAllowAccess_forAdmin() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/users/me - powinien zwrócić dane zalogowanego użytkownika")
    @WithMockUser(username = "test-user")
    void shouldGetCurrentUser() throws Exception {
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login", is("test-user")));
    }

    @Test
    @DisplayName("POST /api/users/trainer/{id} - powinien pomyślnie uczynić użytkownika trenerem")
    @WithMockUser
    void addTrainer_shouldSucceed_whenUserIsNotTrainer() throws Exception {
        // Given
        testUser.setTrainer(null); // Upewniamy się, że użytkownik nie jest jeszcze trenerem
        when(userService.ifUserExists(1L)).thenReturn(true);
        when(userService.findUserById(1L)).thenReturn(testUser);

        Trainer trainerRequestBody = new Trainer();
        trainerRequestBody.setSpecialization("Yoga");

        // When & Then
        mockMvc.perform(post("/api/users/trainer/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerRequestBody)))
                .andExpect(status().isCreated());

        // Weryfikacja, czy rola została zmieniona i czy trener został utworzony
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);

        // Sprawdzamy, czy updateUser został wywołany na userze
        // W twoim kodzie nie ma updateUser, jest tylko zmiana roli, więc weryfikujemy, co jest
        verify(trainerService).createTrainer(trainerCaptor.capture());

        assertThat(testUser.getRoles()).isEqualTo("ROLE_TRAINER");
        assertThat(trainerCaptor.getValue().getSpecialization()).isEqualTo("Yoga");
    }

    @Test
    @DisplayName("POST /api/users/trainer/{id} - powinien zwrócić 409 Conflict, gdy użytkownik jest już trenerem")
    @WithMockUser
    void addTrainer_shouldReturnConflict_whenUserIsAlreadyTrainer() throws Exception {
        // Given
        testUser.setTrainer(new Trainer()); // Użytkownik jest już trenerem
        when(userService.ifUserExists(1L)).thenReturn(true);
        when(userService.findUserById(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users/trainer/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Trainer())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$", is("User is already trainer")));

        //verify(trainerService, never()).createTrainer(any());
    }

    @Test
    @DisplayName("GET /api/users/workout")
    @WithMockUser(username = "test-user")
    void getAllUserWorkouts_shouldReturnNotFound_whenNoWorkouts() throws Exception {
        // Given
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);
        when(userWorkoutService.findByUser_Id(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/users/workout"))
                .andExpect(status().isNotFound());

        when(userWorkoutService.findByUser_Id(1L)).thenReturn(List.of(new User_Workouts()));
        mockMvc.perform(get("/api/users/workout"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/users/workout - powinien utworzyć nowy trening dla zalogowanego użytkownika")
    @WithMockUser(username = "test-user")
    void updateWorkout_shouldCreateWorkout() throws Exception {
        // Given
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);

        WorkoutDTO workoutDTO = new WorkoutDTO();
        workoutDTO.setTitle("Trening siłowy");
        workoutDTO.setDescription("Poniedziałkowy trening klatki piersiowej");

        // When & Then
        mockMvc.perform(post("/api/users/workout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workoutDTO)))
                .andExpect(status().isCreated());

        // Weryfikujemy, czy serwis został wywołany z poprawnymi danymi
        ArgumentCaptor<User_Workouts> workoutCaptor = ArgumentCaptor.forClass(User_Workouts.class);
        verify(userWorkoutService).createUser_Workouts(workoutCaptor.capture());

        assertThat(workoutCaptor.getValue().getUser().getLogin()).isEqualTo("test-user");
        assertThat(workoutCaptor.getValue().getTitle()).isEqualTo("Trening siłowy");
    }

    @Test
    @DisplayName("Pobranie listy wszystkich trenerów")
    @WithMockUser(username = "test-user")
    void getAllTrainers_shouldReturnOk() throws Exception {
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);
        when(trainerService.getAll()).thenReturn(List.of(testTrainer.getTrainer()));
        mockMvc.perform(get("/api/users/trainer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        when(trainerService.getAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/users/trainer"))
                .andExpect(status().isNotFound());
    }

}