package projekt.PD.DataBase.DB_Trainer.Trainer_Service;

import projekt.PD.DataBase.DB_Trainer.Trainer;

import java.util.List;
import java.util.Optional;

/** Interfejs metod związanych z zarządzaniem trenerami */

public interface TrainerService {
    Trainer findBySpecialization(String specialization);
    Optional<Trainer> findById(Long id);
    void deleteById(Long id);
    void createTrainer(Trainer trainer);
    List<Trainer> getAll();
}
