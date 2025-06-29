package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import projekt.PD.Controller.TrainerPlan_Controller.TrainerPlanController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserDTO;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerPlanControllerTest {

    private TrainerPlanService trainerPlanService;
    private UserService userService;
    private CurrentUser currentUser;
    private TrainerPlanController controller;

    private User trainerUser;
    private Trainer trainer;
    private User userToPlan;
    private TrainerPlan trainerPlan;

    @BeforeEach
    void setUp() {
        trainerPlanService = mock(TrainerPlanService.class);
        userService = mock(UserService.class);
        currentUser = mock(CurrentUser.class);
        controller = new TrainerPlanController(trainerPlanService, userService, currentUser);

        trainer = new Trainer();
        trainer.setId(1L);

        trainerUser = new User();
        trainerUser.setId(100L);
        trainerUser.setTrainer(trainer);

        trainer.setUser(trainerUser);

        userToPlan = new User();
        userToPlan.setId(200L);

        trainerPlan = new TrainerPlan();
        trainerPlan.setId(300L);
        trainerPlan.setTrainerPlanUser(userToPlan);
        trainerPlan.setPlanTrainer(trainer);
    }

    @Test
    void getPlanTrainer_ShouldReturnPlansViewAndAddPlansToModel() {
        // given
        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(trainerPlanService.findByPlanTrainer_Id(trainer.getId())).thenReturn(List.of(trainerPlan));
        Model model = new ConcurrentModel();

        // when
        String view = controller.getPlanTrainer(model);

        // then
        assertEquals("TrainingPlanByTrainer/Trainer/trainer-traning-plan", view);
        assertTrue(model.containsAttribute("plans"));
        verify(currentUser).getUserID();
    }

    @Test
    void getPlanTrainer_ShouldAddMessageWhenNoPlans() {
        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(trainerPlanService.findByPlanTrainer_Id(trainer.getId())).thenReturn(List.of());
        Model model = new ConcurrentModel();

        String view = controller.getPlanTrainer(model);

        assertEquals("TrainingPlanByTrainer/Trainer/trainer-traning-plan", view);
        assertEquals("Brak planów od trenera", model.getAttribute("msg"));
    }

    @Test
    void newTrainingPlanForUser_ShouldAddAttributesAndReturnFormView() {
        when(currentUser.getUserID()).thenReturn(trainerUser);
        Model model = new ConcurrentModel();

        String view = controller.newTrainingPlanForUser(model);

        assertEquals("TrainingPlanByTrainer/Trainer/add-training-plan-to-user", view);
        assertTrue(model.containsAttribute("trainerPlanDTO"));
    }

    @Test
    void createTrainingPlanForUser_ShouldRedirect_WhenSuccess() {
        TrainerPlanDTO dto = new TrainerPlanDTO();
        dto.setUser(new UserDTO(userToPlan));

        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(userService.findUserById(userToPlan.getId())).thenReturn(userToPlan);
        when(trainerPlanService.create(any(TrainerPlan.class))).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = controller.createTrainingPlanForUser(dto, model);

        assertEquals("redirect:/trainerplan/trainer", view);
    }

    @Test
    void createTrainingPlanForUser_ShouldReturnForm_WhenErrorOccurs() {
        TrainerPlanDTO dto = new TrainerPlanDTO();
        dto.setUser(new UserDTO(userToPlan));

        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(userService.findUserById(userToPlan.getId())).thenReturn(null); // Simulate user not found

        Model model = new ConcurrentModel();
        String view = controller.createTrainingPlanForUser(dto, model);

        assertEquals("TrainingPlanByTrainer/Trainer/add-training-plan-to-user", view);
    }

    @Test
    void deleteTrainingPlan_ShouldRedirectWithSuccessMessage_WhenDeleted() {
        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(trainerPlanService.findByIdAndPlanTrainer_Id(trainerPlan.getId(), trainer.getId()))
                .thenReturn(Optional.of(trainerPlan));
        when(trainerPlanService.deleteById(trainerPlan.getId())).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = controller.deleteTrainingPlan(trainerPlan.getId(), model);

        assertEquals("redirect:/trainerplan/trainer", view);
        assertEquals("Usunięto plan", model.getAttribute("msg"));
    }

    @Test
    void deleteTrainingPlan_ShouldRedirectWithErrorMessage_WhenNotFoundOrFailed() {
        when(currentUser.getUserID()).thenReturn(trainerUser);
        when(trainerPlanService.findByIdAndPlanTrainer_Id(999L, trainer.getId())).thenReturn(Optional.empty());

        Model model = new ConcurrentModel();
        String view = controller.deleteTrainingPlan(999L, model);

        assertEquals("redirect:/trainerplan/trainer", view);
        assertEquals("Błąd podczas usuwania planu", model.getAttribute("msg"));
    }
}
