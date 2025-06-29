package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import projekt.PD.Controller.Trainers_Controller.TrainerController;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.Services.CurrentUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerControllerTest {

    @InjectMocks
    private TrainerController trainerController;

    @Mock
    private TrainerService trainerService;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private Model model;

    private User mockUser;
    private Trainer mockTrainer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setLogin("testUser");

        mockTrainer = new Trainer();
        mockTrainer.setId(1L);
        mockTrainer.setUser(mockUser);
        mockUser.setTrainer(mockTrainer);

    }

    @Test
    void getAllTrainers_ShouldReturnTrainersView_WhenTrainersExist() {
        List<Trainer> trainers = List.of(mockTrainer);
        when(trainerService.getAll()).thenReturn(trainers);

        String view = trainerController.getAllTrainers(model);

        verify(model).addAttribute(eq("trainers"), anyList());
        assertEquals("Trainer/trainers", view);
    }

    @Test
    void getAllTrainers_ShouldReturnTrainersViewWithError_WhenNoTrainersExist() {
        when(trainerService.getAll()).thenReturn(List.of());

        String view = trainerController.getAllTrainers(model);

        verify(model).addAttribute("error", "No trainers found");
        assertEquals("Trainer/trainers", view);
    }

    @Test
    void getAddTrainerPage_ShouldReturnBecomeTrainerView() {
        String view = trainerController.getAddTrainerPage(model);

        verify(model).addAttribute(eq("trainer"), any(Trainer.class));
        assertEquals("Trainer/become-trainer", view);
    }

    @Test
    void addTrainer_ShouldReturnErrorView_WhenUserIsAlreadyTrainer() {
        Trainer existingTrainer = new Trainer();
        mockUser.setTrainer(existingTrainer);
        when(currentUser.getUserID()).thenReturn(mockUser);

        String view = trainerController.addTrainer(new Trainer(), model);

        verify(model).addAttribute("error", "User is already a trainer");
        assertEquals("error", view);
    }

    @Test
    void addTrainer_ShouldCreateTrainerAndReturnHomeView_WhenUserNotTrainer() {
        mockUser.setTrainer(null);
        when(currentUser.getUserID()).thenReturn(mockUser);

        Trainer newTrainer = new Trainer();

        String view = trainerController.addTrainer(newTrainer, model);

        verify(trainerService).createTrainer(any(Trainer.class));
        verify(model).addAttribute("message", "User is now a Trainer");
        assertEquals("User/Information/home", view);
    }
}
