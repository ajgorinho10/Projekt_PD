package projekt.PD.DataBase.DB_Trainer;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;

import java.util.List;
import java.util.Optional;

/** Interfejs związanych z tabelą trainers zapytań do bazy danych */

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Trainer findBySpecialization(String specialization);
    Optional<Trainer> findById( Long id);
}
