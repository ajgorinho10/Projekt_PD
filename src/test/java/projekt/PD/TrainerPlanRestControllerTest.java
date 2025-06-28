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
import projekt.PD.Controller.Api.TrainerPlanRestController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerPlanRestController.class)
@Import(TrainerPlanRestControllerTest.TestConfig.class)
class TrainerPlanRestControllerTest {

    // Nowoczesna konfiguracja mocków dla tego testu
    static class TestConfig {
        @Bean
        public TrainerPlanService trainerPlanService() {
            return Mockito.mock(TrainerPlanService.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TrainerPlanService trainerPlanService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User testTrainerUser;
    private Trainer testTrainer;
    private TrainerPlan testPlan;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("test-user");

        testTrainer = new Trainer();
        testTrainer.setId(10L);

        testTrainerUser = new User();
        testTrainerUser.setId(2L);
        testTrainerUser.setLogin("trainer-user");
        testTrainerUser.setTrainer(testTrainer);
        testTrainer.setUser(testTrainerUser);

        testPlan = new TrainerPlan();
        testPlan.setId(100L);
        testPlan.setTitle("Plan od trenera");
        testPlan.setTrainerPlanUser(testUser);
        testPlan.setPlanTrainer(testTrainer);
    }

    @Test
    @DisplayName("GET /api/trainerPlan/user - powinien zwrócić plany zalogowanego użytkownika")
    @WithMockUser(username = "test-user")
    void shouldGetTrainerPlansForCurrentUser() throws Exception {
        // Given
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);
        when(trainerPlanService.findByTrainerPlanUser_Id(1L)).thenReturn(List.of(testPlan));

        // When & Then
        mockMvc.perform(get("/api/trainerPlan/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Plan od trenera")));
    }

    @Test
    @DisplayName("GET /api/trainerPlan/user - powinien zwrócić 404, gdy użytkownik nie ma planów")
    @WithMockUser(username = "test-user")
    void shouldReturnNotFoundWhenUserHasNoPlans() throws Exception {
        // Given
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);
        // Zgodnie z rekomendacją, serwis powinien zwracać pustą listę, a kontroler na tej podstawie 404
        when(trainerPlanService.findByTrainerPlanUser_Id(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/trainerPlan/user"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/trainerPlan/user/{id} - użytkownik powinien móc usunąć swój plan")
    @WithMockUser(username = "test-user")
    void shouldDeleteUserTrainingPlan() throws Exception {
        // Given
        when(userService.findUserByLogin("test-user")).thenReturn(testUser);
        when(trainerPlanService.findByIdAndTrainerPlanUser_Id(100L, 1L)).thenReturn(Optional.of(testPlan));

        // When & Then
        mockMvc.perform(delete("/api/trainerPlan/user/100").with(csrf()))
                .andExpect(status().isOk());

        // Verify
        verify(trainerPlanService).deleteById(100L);
    }

    @Test
    @DisplayName("POST /api/trainerPlan/trainer/{id} - trener powinien móc stworzyć plan dla użytkownika")
    @WithMockUser(username = "trainer-user", roles = "TRAINER")
    void shouldCreateTrainerPlanForUser() throws Exception {
        // Given
        when(userService.findUserByLogin("trainer-user")).thenReturn(testTrainerUser);
        when(userService.findUserById(1L)).thenReturn(testUser);
        // Symulujemy, że serwis zwraca true po pomyślnym utworzeniu
        when(trainerPlanService.create(any(TrainerPlan.class))).thenReturn(true);

        TrainerPlanDTO dto = new TrainerPlanDTO();
        dto.setTitle("Nowy plan na masę");
        dto.setDescription("Opis nowego planu");

        // When & Then
        mockMvc.perform(post("/api/trainerPlan/trainer/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Verify
        verify(trainerPlanService).create(any(TrainerPlan.class));
    }
}