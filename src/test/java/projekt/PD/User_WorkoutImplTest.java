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
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.User_WorkoutImpl;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;
import projekt.PD.DataBase.DB_UserWorkout.User_WorkoutsRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class User_WorkoutImplTest {

    @Mock
    private User_WorkoutsRepository user_workoutsRepository;

    @InjectMocks
    private User_WorkoutImpl userWorkoutService;

    private User user1;
    private User user2;
    private User_Workouts workout1;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setLogin("user1");

        user2 = new User();
        user2.setId(2L);
        user2.setLogin("user2");

        workout1 = new User_Workouts();
        workout1.setId(100L);
        workout1.setTitle("Trening A");
        workout1.setUser(user1);
    }

    @Test
    @DisplayName("Powinien zwrócić listę treningów dla podanego ID użytkownika")
    void shouldReturnWorkoutsForGivenUserId() {

        when(user_workoutsRepository.findByUser_Id(1L)).thenReturn(List.of(workout1));

        List<User_Workouts> result = userWorkoutService.findByUser_Id(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Trening A");
        verify(user_workoutsRepository).findByUser_Id(1L);
    }

    @Test
    @DisplayName("Powinien zwrócić trening, jeśli użytkownik jest jego właścicielem")
    void shouldReturnWorkoutWhenUserIsOwner() {

        when(user_workoutsRepository.findById(100L)).thenReturn(Optional.of(workout1));

        Optional<User_Workouts> result = userWorkoutService.findById(100L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(workout1);
    }

    @Test
    @DisplayName("Powinien zwrócić pusty Optional, jeśli użytkownik nie jest właścicielem treningu")
    void shouldReturnEmptyWhenUserIsNotOwnerForFindById() {

        when(user_workoutsRepository.findById(100L)).thenReturn(Optional.of(workout1));

        Optional<User_Workouts> result = userWorkoutService.findById(100L, 2L); // Użytkownik 2 próbuje uzyskać dostęp

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Powinien usunąć trening i zwrócić true, jeśli użytkownik jest właścicielem")
    void shouldDeleteAndReturnTrueWhenUserIsOwner() {

        when(user_workoutsRepository.findById(100L)).thenReturn(Optional.of(workout1));

        boolean result = userWorkoutService.deleteById(100L, 1L);

        assertThat(result).isTrue();
        verify(user_workoutsRepository).deleteById(100L);
    }

    @Test
    @DisplayName("Nie powinien usuwać treningu i zwrócić false, jeśli użytkownik nie jest właścicielem")
    void shouldNotDeleteAndReturnFalseWhenUserIsNotOwner() {

        when(user_workoutsRepository.findById(100L)).thenReturn(Optional.of(workout1));

        boolean result = userWorkoutService.deleteById(100L, 2L);

        assertThat(result).isFalse();
        verify(user_workoutsRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Powinien zapisać nowy trening, jeśli ID jest null")
    void shouldSaveNewWorkoutWhenIdIsNull() {

        User_Workouts newWorkout = new User_Workouts();
        newWorkout.setUser(user1);
        newWorkout.setTitle("Nowy trening");

        userWorkoutService.createUser_Workouts(newWorkout);

        verify(user_workoutsRepository).save(newWorkout);
    }

    @Test
    @DisplayName("Powinien zaktualizować istniejący trening, jeśli użytkownik jest właścicielem")
    void shouldUpdateExistingWorkoutWhenUserIsOwner() {

        when(user_workoutsRepository.findById(100L)).thenReturn(Optional.of(workout1));
        workout1.setTitle("Zaktualizowany tytuł");

        userWorkoutService.createUser_Workouts(workout1);

        verify(user_workoutsRepository).save(workout1);
    }

    @Test
    @DisplayName("Powinien stworzyć nowy trening (klon), gdy użytkownik próbuje zaktualizować cudzy trening")
    void shouldCreateNewWorkoutAsCloneWhenUserIsNotOwner() {
        when(user_workoutsRepository.findById(100L)).thenReturn(Optional.of(workout1));

        User_Workouts attemptToUpdate = new User_Workouts();
        attemptToUpdate.setId(100L);
        attemptToUpdate.setUser(user2);
        attemptToUpdate.setTitle(workout1.getTitle());
        attemptToUpdate.setDescription(workout1.getDescription());
        attemptToUpdate.setDate(workout1.getDate());

        userWorkoutService.createUser_Workouts(attemptToUpdate);

        ArgumentCaptor<User_Workouts> workoutCaptor = ArgumentCaptor.forClass(User_Workouts.class);
        verify(user_workoutsRepository).save(workoutCaptor.capture());

        User_Workouts savedWorkout = workoutCaptor.getValue();
        assertThat(savedWorkout.getId()).isNull();
        assertThat(savedWorkout.getUser()).isEqualTo(user2);
        assertThat(savedWorkout.getTitle()).isEqualTo("Trening A");
    }
}