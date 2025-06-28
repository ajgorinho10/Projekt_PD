package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlanRepository;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanServiceImpl;
import projekt.PD.DataBase.DB_User.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerPlanServiceImplTest {

    @Mock
    private TrainerPlanRepository trainerPlanRepository;

    @InjectMocks
    private TrainerPlanServiceImpl trainerPlanService;

    private User user;
    private Trainer trainer;
    private TrainerPlan plan;

    @BeforeEach
    void setUp() {
        // Przygotowanie wspólnych danych testowych
        user = new User();
        user.setId(1L);

        trainer = new Trainer();
        trainer.setId(10L);

        plan = new TrainerPlan();
        plan.setId(100L);
        plan.setPlanTrainer(trainer);
        plan.setTrainerPlanUser(user);
        plan.setTitle("Plan od trenera");
    }

    @Test
    @DisplayName("findById powinien wywołać repozytorium i zwrócić wynik")
    void findById_shouldCallRepositoryAndReturnResult() {
        // Given
        when(trainerPlanRepository.findById(100L)).thenReturn(Optional.of(plan));

        // When
        Optional<TrainerPlan> result = trainerPlanService.findById(100L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(plan);
        verify(trainerPlanRepository).findById(100L);
    }

    @Test
    @DisplayName("findByPlanTrainer_IdAndTrainerPlanUser_Id powinien wywołać repozytorium i zwrócić wynik")
    void findByPlanTrainer_IdAndTrainerPlanUser_Id_shouldCallRepository() {
        // Given
        when(trainerPlanRepository.findByPlanTrainer_IdAndTrainerPlanUser_Id(10L, 1L)).thenReturn(Optional.of(plan));

        // When
        Optional<TrainerPlan> result = trainerPlanService.findByPlanTrainer_IdAndTrainerPlanUser_Id(10L, 1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(plan);
        verify(trainerPlanRepository).findByPlanTrainer_IdAndTrainerPlanUser_Id(10L, 1L);
    }

    @Test
    @DisplayName("findByPlanTrainer_Id powinien wywołać repozytorium i zwrócić listę")
    void findByPlanTrainer_Id_shouldCallRepositoryAndReturnList() {
        // Given
        when(trainerPlanRepository.findByPlanTrainer_Id(10L)).thenReturn(List.of(plan));

        // When
        List<TrainerPlan> result = trainerPlanService.findByPlanTrainer_Id(10L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(plan);
        verify(trainerPlanRepository).findByPlanTrainer_Id(10L);
    }

    @Test
    @DisplayName("findByTrainerPlanUser_Id powinien wywołać repozytorium i zwrócić listę")
    void findByTrainerPlanUser_Id_shouldCallRepositoryAndReturnList() {
        // Given
        when(trainerPlanRepository.findByTrainerPlanUser_Id(1L)).thenReturn(List.of(plan));

        // When
        List<TrainerPlan> result = trainerPlanService.findByTrainerPlanUser_Id(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(plan);
        verify(trainerPlanRepository).findByTrainerPlanUser_Id(1L);
    }

    @Test
    @DisplayName("deleteById powinien wywołać repozytorium.deleteById i zwrócić true")
    void deleteById_shouldCallRepositoryDeleteAndReturnTrue() {
        // Given
        // Ustawiamy, że metoda deleteById nic nie robi (void)
        doNothing().when(trainerPlanRepository).deleteById(100L);

        // When
        boolean result = trainerPlanService.deleteById(100L);

        // Then
        assertThat(result).isTrue();
        verify(trainerPlanRepository).deleteById(100L);
    }

    @Test
    @DisplayName("create powinien wywołać repozytorium.save i zwrócić true")
    void create_shouldCallRepositorySaveAndReturnTrue() {
        // Given
        // Ustawiamy, że metoda save zwraca przekazany obiekt
        when(trainerPlanRepository.save(plan)).thenReturn(plan);

        // When
        boolean result = trainerPlanService.create(plan);

        // Then
        assertThat(result).isTrue();
        verify(trainerPlanRepository).save(plan);
    }
}