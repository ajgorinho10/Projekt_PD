package projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service;

import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;

import java.util.List;
import java.util.Optional;

public interface TrainerPlanService {
    Optional<TrainerPlan> findById(Long id);
    Optional<TrainerPlan> findByPlanTrainer_IdAndTrainerPlanUser_Id(Long trainerPlanId, Long userId);
    Optional<TrainerPlan> findByIdAndTrainerPlanUser_Id(Long trainerPlanId, Long userId);
    Optional<TrainerPlan> findByIdAndPlanTrainer_Id(Long trainerPlanId, Long id);

    List<TrainerPlan> findByPlanTrainer_Id(Long id);
    List<TrainerPlan> findByTrainerPlanUser_Id(Long id);

    boolean deleteById(Long id);
    boolean create(TrainerPlan trainerPlan);
}
