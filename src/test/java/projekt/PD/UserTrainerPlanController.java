package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import projekt.PD.Controller.TrainerPlan_Controller.UserTrainerPlanController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTrainerPlanControllerUnitTest {

    private TrainerPlanService trainerPlanService;
    private UserService userService;
    private CurrentUser currentUser;
    private UserTrainerPlanController controller;
    private User testUser;
    private TrainerPlan testPlan;
    private User testTrainerUser;
    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        trainerPlanService = mock(TrainerPlanService.class);
        userService = mock(UserService.class);
        currentUser = mock(CurrentUser.class);
        controller = new UserTrainerPlanController(trainerPlanService, userService, currentUser);

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testUser");

        testTrainerUser = new User();
        testTrainerUser.setId(2L);
        testTrainerUser.setLogin("trainerUser");

        testTrainer = new Trainer();
        testTrainer.setId(10L); // musi być ustawione
        testTrainer.setUser(testTrainerUser);
        testTrainerUser.setTrainer(testTrainer);

        testPlan = new TrainerPlan();
        testPlan.setId(101L);
        testPlan.setTitle("Test Plan");
        testPlan.setTrainerPlanUser(testUser);
        testPlan.setPlanTrainer(testTrainer);

        when(currentUser.getUserID()).thenReturn(testUser);
    }

    @Test
    void getTrainerPlan_ShouldAddPlansToModel_AndReturnView() {
        // Test sprawdza, czy widok z listą planów jest poprawnie zwracany, a model zawiera plany
        when(trainerPlanService.findByTrainerPlanUser_Id(testUser.getId())).thenReturn(List.of(testPlan));

        Model model = new ConcurrentModel();
        String viewName = controller.getTrainerPlan(model);

        assertEquals("TrainingPlanByTrainer/User/user-training-plan-from-trainer", viewName);
        assertTrue(model.containsAttribute("plans"));
        List<TrainerPlanDTO> dtos = (List<TrainerPlanDTO>) model.getAttribute("plans");
        assertEquals(1, dtos.size());
        assertEquals("Test Plan", dtos.get(0).getTitle());
    }

    @Test
    void getTrainerPlan_ShouldAddErrorMessage_WhenNoPlansFound() {
        // Test sprawdza, czy w przypadku braku planów dodany zostanie komunikat o błędzie
        when(trainerPlanService.findByTrainerPlanUser_Id(testUser.getId())).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String viewName = controller.getTrainerPlan(model);

        assertEquals("TrainingPlanByTrainer/User/user-training-plan-from-trainer", viewName);
        assertTrue(model.containsAttribute("error"));
        assertEquals("Brak planów", model.getAttribute("error"));
    }

    @Test
    void getUserTrainingPlan_ShouldReturnDetailsView_WhenPlanExists() {
        // Test sprawdza, czy szczegóły planu są poprawnie dodane do modelu
        when(trainerPlanService.findByIdAndTrainerPlanUser_Id(testPlan.getId(), testUser.getId()))
                .thenReturn(Optional.of(testPlan));

        Model model = new ConcurrentModel();
        String viewName = controller.getUserTrainingPlan(testPlan.getId(), model);

        assertEquals("TrainingPlanByTrainer/User/user-training-plan-from-trainer-details", viewName);
        assertTrue(model.containsAttribute("plan"));
        TrainerPlanDTO dto = (TrainerPlanDTO) model.getAttribute("plan");
        assertEquals("Test Plan", dto.getTitle());
    }

    @Test
    void getUserTrainingPlan_ShouldAddErrorMessage_WhenPlanNotFound() {
        // Test sprawdza, czy przy braku planu wyświetlany jest komunikat o błędzie
        when(trainerPlanService.findByIdAndTrainerPlanUser_Id(999L, testUser.getId())).thenReturn(Optional.empty());

        Model model = new ConcurrentModel();
        String viewName = controller.getUserTrainingPlan(999L, model);

        assertEquals("TrainingPlanByTrainer/User/user-training-plan-from-trainer-details", viewName);
        assertTrue(model.containsAttribute("msg"));
        assertEquals("Błąd wyświetlania planu", model.getAttribute("msg"));
    }

    @Test
    void deleteUserTrainingPlan_ShouldRedirectAndAddSuccessMessage_WhenPlanDeleted() {
        // Test sprawdza poprawne usunięcie planu i komunikat sukcesu
        when(trainerPlanService.findByIdAndTrainerPlanUser_Id(testPlan.getId(), testUser.getId()))
                .thenReturn(Optional.of(testPlan));
        when(trainerPlanService.deleteById(testPlan.getId())).thenReturn(true);

        Model model = new ConcurrentModel();
        String viewName = controller.deleteUserTrainingPlan(testPlan.getId(), model);

        assertEquals("redirect:/trainerplan/user", viewName);
        assertEquals("Usunięto plan", model.getAttribute("msg"));
    }

    @Test
    void deleteUserTrainingPlan_ShouldRedirectAndAddErrorMessage_WhenDeleteFails() {
        // Test sprawdza sytuację, gdy plan nie zostanie usunięty (np. nie istnieje)
        when(trainerPlanService.findByIdAndTrainerPlanUser_Id(testPlan.getId(), testUser.getId()))
                .thenReturn(Optional.of(testPlan));
        when(trainerPlanService.deleteById(testPlan.getId())).thenReturn(false);

        Model model = new ConcurrentModel();
        String viewName = controller.deleteUserTrainingPlan(testPlan.getId(), model);

        assertEquals("redirect:/trainerplan/user", viewName);
        assertEquals("Błąd usuwania planu", model.getAttribute("msg"));
    }
}
