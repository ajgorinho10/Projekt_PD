package projekt.PD.DataBase.DB_TrainerPlan;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerPlanRepository extends CrudRepository<TrainerPlan, Long> {
    Optional<TrainerPlan> findById(Long id);
    Optional<TrainerPlan> findByPlanTrainer_IdAndTrainerPlanUser_Id(Long trainerPlanId, Long userId);
    Optional<TrainerPlan> findByIdAndTrainerPlanUser_Id(Long userId,Long trainerPlanUserId);
    Optional<TrainerPlan> findByIdAndPlanTrainer_Id(Long id,Long trainerPlanId);

    List<TrainerPlan> findByPlanTrainer_Id(Long id);
    List<TrainerPlan> findByTrainerPlanUser_Id(Long id);

    void deleteById(Long id);

}
