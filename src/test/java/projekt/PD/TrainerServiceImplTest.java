package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.TrainerRepository;
import projekt.PD.DataBase.DB_Trainer.Trainer_Service.TrainerServiceImpl;
import projekt.PD.DataBase.DB_User.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock // Tworzymy zaślepkę (mocka) dla repozytorium
    private TrainerRepository trainerRepository;

    @InjectMocks // Tworzymy instancję serwisu i wstrzykujemy do niej mocka
    private TrainerServiceImpl trainerService;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        // Przygotowujemy obiekt testowy, który będzie używany w wielu testach
        User user = new User();
        user.setId(1L);

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
        trainer.setSpecialization("CrossFit");
    }

    @Test
    @DisplayName("Powinien znaleźć trenera po specjalizacji")
    void shouldFindTrainerBySpecialization() {
        // Given (Mając)
        // Definiujemy, że gdy repozytorium zostanie zapytane o specjalizację "CrossFit", ma zwrócić naszego trenera
        when(trainerRepository.findBySpecialization("CrossFit")).thenReturn(trainer);

        // When (Kiedy)
        // Wywołujemy testowaną metodę serwisu
        Trainer foundTrainer = trainerService.findBySpecialization("CrossFit");

        // Then (Wtedy)
        // Sprawdzamy, czy wynik jest zgodny z oczekiwaniami
        assertThat(foundTrainer).isNotNull();
        assertThat(foundTrainer.getSpecialization()).isEqualTo("CrossFit");
        // Weryfikujemy, czy metoda repozytorium została wywołana dokładnie raz z poprawnym argumentem
        verify(trainerRepository).findBySpecialization("CrossFit");
    }

    @Test
    @DisplayName("Powinien znaleźć trenera po ID")
    void shouldFindTrainerById() {
        // Given
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        // When
        Optional<Trainer> foundTrainer = trainerService.findById(1L);

        // Then
        assertThat(foundTrainer).isPresent();
        assertThat(foundTrainer.get()).isEqualTo(trainer);
        verify(trainerRepository).findById(1L);
    }

    @Test
    @DisplayName("Powinien wywołać metodę deleteById na repozytorium")
    void shouldDeleteTrainerById() {
        // Given
        // Dla metod void nie musimy nic zwracać, Mockito domyślnie nic nie robi

        // When
        trainerService.deleteById(1L);

        // Then
        // Weryfikujemy, że metoda deleteById została wywołana na repozytorium z poprawnym ID
        verify(trainerRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Powinien wywołać metodę save na repozytorium podczas tworzenia trenera")
    void shouldCreateTrainer() {
        // Given
        // Dla metod void nie musimy nic zwracać

        // When
        trainerService.createTrainer(trainer);

        // Then
        // Weryfikujemy, że metoda save została wywołana z naszym obiektem trenera
        verify(trainerRepository).save(trainer);
    }

    @Test
    @DisplayName("Powinien zwrócić listę wszystkich trenerów")
    void shouldGetAllTrainers() {
        // Given
        // Definiujemy, że repozytorium ma zwrócić listę zawierającą naszego jednego trenera
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        // When
        List<Trainer> trainers = trainerService.getAll();

        // Then
        assertThat(trainers).isNotNull();
        assertThat(trainers).hasSize(1);
        assertThat(trainers.get(0).getSpecialization()).isEqualTo("CrossFit");
        verify(trainerRepository).findAll();
    }
}