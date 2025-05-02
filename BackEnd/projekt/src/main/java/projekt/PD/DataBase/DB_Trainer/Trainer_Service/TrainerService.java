package projekt.PD.DataBase.DB_Trainer.Trainer_Service;

import projekt.PD.DataBase.DB_Trainer.Trainer;

import java.util.List;

public interface TrainerService {
    Trainer findBySpecialization(String specialization);
    Trainer findById(int id);
    void deleteById(int id);
    void createTrainer(Trainer trainer);
}
