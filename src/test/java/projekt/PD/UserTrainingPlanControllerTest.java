package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import projekt.PD.Controller.TrainingPlan_Controller.UserTrainingPlanController;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanDTO;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanService;
import projekt.PD.Services.CurrentUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserTrainingPlanControllerTest {

    @InjectMocks
    private UserTrainingPlanController controller;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private UserTrainingPlanService userTrainingPlanService;

    @Mock
    private Model model;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setLogin("testUser");

        when(currentUser.getUserID()).thenReturn(mockUser);
    }

    @Test
    void getAllUser_s_ShouldReturnPlansView_WhenPlansExist() {
        List<UserTrainingPlan> plans = List.of(new UserTrainingPlan());
        when(userTrainingPlanService.findByUser_Id(mockUser.getId())).thenReturn(plans);

        String view = controller.getAllUser_s(model);

        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute(eq("plans"), any());
        assertEquals("TrainingPlan/Users/user-training-plans", view);
    }

    @Test
    void getAllUser_s_ShouldReturnErrorView_WhenNoPlansExist() {
        when(userTrainingPlanService.findByUser_Id(mockUser.getId())).thenReturn(List.of());

        String view = controller.getAllUser_s(model);

        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute("error", "No training plans found");
        assertEquals("TrainingPlan/Users/user-training-plans", view);
    }

    @Test
    void getAllUser_Trainers_ShouldReturnDetailsView_WhenPlanFound() {
        UserTrainingPlan plan = new UserTrainingPlan();
        when(userTrainingPlanService.findById(1L, mockUser.getId())).thenReturn(Optional.of(plan));

        String view = controller.getAllUser_Trainers(1L, model);

        verify(model).addAttribute(eq("plan"), any(UserTrainingPlanDTO.class));
        verify(model).addAttribute("user", mockUser);
        assertEquals("TrainingPlan/Users/user-training-plan-details", view);
    }

    @Test
    void getAllUser_Trainers_ShouldReturnDetailsViewWithError_WhenPlanNotFound() {
        when(userTrainingPlanService.findById(1L, mockUser.getId())).thenReturn(Optional.empty());

        String view = controller.getAllUser_Trainers(1L, model);

        verify(model).addAttribute("error", "Training plan not found");
        assertEquals("TrainingPlan/Users/user-training-plan-details", view);
    }

    @Test
    void showCreateTrainingPlanForm_ShouldReturnCreateFormView() {
        String view = controller.showCreateTrainingPlanForm(model);

        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute(eq("trainerPlanDTO"), any(UserTrainingPlanDTO.class));
        assertEquals("TrainingPlan/Users/create-training-plan", view);
    }

    @Test
    void updateTrainingPlan_ShouldRedirectToTrainingPlanList() {
        UserTrainingPlan plan = new UserTrainingPlan();

        String view = controller.updateTrainingPlan(plan, model);

        verify(userTrainingPlanService).create_or_change(plan);
        assertEquals("redirect:/trainingplan/user", view);
    }

    @Test
    void deleteTrainingPlan_ShouldRedirectWithMessage_WhenDeletionSucceeds() {
        when(userTrainingPlanService.deleteById(1L, mockUser.getId())).thenReturn(true);

        String view = controller.deleteTrainingPlan(1L, model);

        verify(model).addAttribute("message", "Training has been deleted");
        assertEquals("redirect:/trainingplan/user", view);
    }

    @Test
    void deleteTrainingPlan_ShouldRedirectWithError_WhenDeletionFails() {
        when(userTrainingPlanService.deleteById(1L, mockUser.getId())).thenReturn(false);

        String view = controller.deleteTrainingPlan(1L, model);

        verify(model).addAttribute("error", "There was a problem when deleting the training plan");
        assertEquals("redirect:/trainingplan/user", view);
    }
}
