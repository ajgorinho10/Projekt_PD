package projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service;

import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;

import java.util.List;
import java.util.Optional;

/** Interfejs metod związanych z zarządzaniem planami treningowymi od trenerów */

public interface TrainerPlanService {
    Optional<TrainerPlan> findById(Long id);
    Optional<TrainerPlan> findByPlanTrainer_IdAndTrainerPlanUser_Id(Long trainerPlanId, Long userId);
    Optional<TrainerPlan> findByIdAndTrainerPlanUser_Id(Long Id,Long trainerPlanUserId);
    Optional<TrainerPlan> findByIdAndPlanTrainer_Id(Long id,Long trainerPlanId);

    List<TrainerPlan> findByPlanTrainer_Id(Long id);
    List<TrainerPlan> findByTrainerPlanUser_Id(Long id);

    boolean deleteById(Long id);
    boolean create(TrainerPlan trainerPlan);
}
