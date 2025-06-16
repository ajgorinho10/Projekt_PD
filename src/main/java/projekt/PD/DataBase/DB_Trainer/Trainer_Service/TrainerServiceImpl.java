package projekt.PD.DataBase.DB_Trainer.Trainer_Service;

import org.springframework.stereotype.Service;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.TrainerRepository;

import java.util.List;
import java.util.Optional;

/** Implementacja metod związanych z zarządzaniem kontami trenerów */

@Service
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;

    public TrainerServiceImpl(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    public Trainer findBySpecialization(String specialization) {
        return trainerRepository.findBySpecialization(specialization);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return trainerRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        trainerRepository.deleteById(id);
    }

    @Override
    public void createTrainer(Trainer trainer) {
        trainerRepository.save(trainer);
    }

    @Override
    public List<Trainer> getAll() {
        return trainerRepository.findAll();
    }
}
