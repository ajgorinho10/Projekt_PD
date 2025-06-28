package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlanRepository;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service.UserTrainingPlanServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTrainingPlanServiceImplTest {

    @Mock
    private UserTrainingPlanRepository userTrainingPlanRepository;

    @InjectMocks
    private UserTrainingPlanServiceImpl userTrainingPlanService;

    private User user1;
    private User user2;
    private UserTrainingPlan plan1;

    @BeforeEach
    void setUp() {
        // Przygotowanie danych testowych
        user1 = new User();
        user1.setId(1L);
        user1.setLogin("user1");

        user2 = new User();
        user2.setId(2L);
        user2.setLogin("user2");

        plan1 = new UserTrainingPlan();
        plan1.setId(100L);
        plan1.setTitle("Plan siłowy");
        plan1.setUser(user1);
        plan1.setMonday("Klatka");
    }

    @Test
    @DisplayName("isUserTrainingPlan powinien zwrócić true, gdy użytkownik jest właścicielem")
    void isUserTrainingPlan_shouldReturnTrue_whenUserIsOwner() {
        // Given
        when(userTrainingPlanRepository.findById(100L)).thenReturn(Optional.of(plan1));

        // When
        boolean result = userTrainingPlanService.isUserTrainingPlan(100L, 1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isUserTrainingPlan powinien zwrócić false, gdy użytkownik nie jest właścicielem")
    void isUserTrainingPlan_shouldReturnFalse_whenUserIsNotOwner() {
        // Given
        when(userTrainingPlanRepository.findById(100L)).thenReturn(Optional.of(plan1));

        // When
        boolean result = userTrainingPlanService.isUserTrainingPlan(100L, 2L); // Użytkownik 2

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("deleteById powinien usunąć plan i zwrócić true, gdy użytkownik jest właścicielem")
    void deleteById_shouldDeleteAndReturnTrue_whenUserIsOwner() {
        // Given
        when(userTrainingPlanRepository.existsById(100L)).thenReturn(true);
        // Musimy też zamockować wewnętrzne wywołanie isUserTrainingPlan
        when(userTrainingPlanRepository.findById(100L)).thenReturn(Optional.of(plan1));

        // When
        boolean result = userTrainingPlanService.deleteById(100L, 1L);

        // Then
        assertThat(result).isTrue();
        verify(userTrainingPlanRepository).deleteById(100L);
    }

    @Test
    @DisplayName("deleteById nie powinien usuwać planu i zwrócić false, gdy użytkownik nie jest właścicielem")
    void deleteById_shouldNotDeleteAndReturnFalse_whenUserIsNotOwner() {
        // Given
        when(userTrainingPlanRepository.existsById(100L)).thenReturn(true);
        when(userTrainingPlanRepository.findById(100L)).thenReturn(Optional.of(plan1));

        // When
        boolean result = userTrainingPlanService.deleteById(100L, 2L);

        // Then
        assertThat(result).isFalse();
        verify(userTrainingPlanRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("create_or_change powinien zapisać ORYGINALNY obiekt, gdy ID jest null")
    void createOrChange_shouldSaveOriginalObject_whenIdIsNull() {
        // Ten test ujawnia nietypową logikę w kodzie
        // Given
        UserTrainingPlan newPlan = new UserTrainingPlan();
        newPlan.setUser(user1);
        newPlan.setTitle("Nowy plan");

        // When
        userTrainingPlanService.create_or_change(newPlan);

        // Then
        // Weryfikujemy, że zapisany został DOKŁADNIE ten sam obiekt, który przekazaliśmy
        verify(userTrainingPlanRepository).save(newPlan);
        // I że metoda save została wywołana tylko raz (nie jest wywoływana na końcu metody)
        verify(userTrainingPlanRepository, times(1)).save(any(UserTrainingPlan.class));
    }

    @Test
    @DisplayName("create_or_change powinien zaktualizować istniejący plan, gdy użytkownik jest właścicielem")
    void createOrChange_shouldUpdateExistingPlan_whenUserIsOwner() {
        // Given
        when(userTrainingPlanRepository.findById(100L)).thenReturn(Optional.of(plan1));

        UserTrainingPlan updatedPlanData = new UserTrainingPlan();
        updatedPlanData.setId(100L);
        updatedPlanData.setUser(user1);
        updatedPlanData.setTitle("Nowy Tytuł Planu");
        updatedPlanData.setMonday("Plecy");

        // When
        userTrainingPlanService.create_or_change(updatedPlanData);

        // Then
        ArgumentCaptor<UserTrainingPlan> planCaptor = ArgumentCaptor.forClass(UserTrainingPlan.class);
        verify(userTrainingPlanRepository).save(planCaptor.capture());

        UserTrainingPlan savedPlan = planCaptor.getValue();
        assertThat(savedPlan.getId()).isEqualTo(100L);
        assertThat(savedPlan.getTitle()).isEqualTo("Nowy Tytuł Planu");
        assertThat(savedPlan.getMonday()).isEqualTo("Plecy");
        assertThat(savedPlan.getUser()).isEqualTo(user1);
    }

    @Test
    @DisplayName("create_or_change powinien stworzyć nowy plan (klon), gdy użytkownik nie jest właścicielem")
    void createOrChange_shouldCreateClone_whenUserIsNotOwner() {
        // Given
        // Użytkownik 2 próbuje "zaktualizować" plan użytkownika 1
        UserTrainingPlan attemptToUpdateByOtherUser = new UserTrainingPlan();
        attemptToUpdateByOtherUser.setId(100L); // ID istniejącego planu
        attemptToUpdateByOtherUser.setUser(user2); // Ale z nowym użytkownikiem
        attemptToUpdateByOtherUser.setTitle("Tytuł sklonowany");

        when(userTrainingPlanRepository.findById(100L)).thenReturn(Optional.of(plan1));

        // When
        userTrainingPlanService.create_or_change(attemptToUpdateByOtherUser);

        // Then
        // Oczekujemy, że zostanie zapisany NOWY obiekt z danymi skopiowanymi, ale bez ID
        ArgumentCaptor<UserTrainingPlan> planCaptor = ArgumentCaptor.forClass(UserTrainingPlan.class);
        verify(userTrainingPlanRepository).save(planCaptor.capture());

        UserTrainingPlan savedPlan = planCaptor.getValue();
        assertThat(savedPlan.getId()).isNull(); // Kluczowe: ID jest null, więc to nowy wpis
        assertThat(savedPlan.getUser()).isEqualTo(user2); // Właścicielem jest użytkownik 2
        assertThat(savedPlan.getTitle()).isEqualTo("Tytuł sklonowany");
    }
}