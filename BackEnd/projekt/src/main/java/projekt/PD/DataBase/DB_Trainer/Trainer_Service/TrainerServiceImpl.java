package projekt.PD.DataBase.DB_Trainer.Trainer_Service;

import org.springframework.stereotype.Service;
import projekt.PD.DataBase.DB_Trainer.Trainer;
import projekt.PD.DataBase.DB_Trainer.TrainerRepository;

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
    public Trainer findById(int id) {
        return trainerRepository.findById(id);
    }

    @Override
    public void deleteById(int id) {
        trainerRepository.deleteById(id);
    }

    @Override
    public void createTrainer(Trainer trainer) {
        trainerRepository.save(trainer);
    }
}
