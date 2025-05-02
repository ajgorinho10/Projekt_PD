package projekt.PD.DataBase.DB_Trainer;

import org.springframework.data.jpa.repository.JpaRepository;
import projekt.PD.DataBase.DB_User.User;

public interface TrainerRepository extends JpaRepository<Trainer, Integer> {
    Trainer findBySpecialization(String specialization);
    Trainer findById(int id);
}
