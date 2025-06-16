package projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service;

import org.springframework.stereotype.Service;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlanRepository;

import java.util.List;
import java.util.Optional;

/** Implementacja metod związanych z zarządzaniem planami treningowymi od trenerów */

@Service
public class TrainerPlanServiceImpl implements TrainerPlanService {

    private final TrainerPlanRepository trainerPlanRepository;

    public TrainerPlanServiceImpl(TrainerPlanRepository trainerPlanRepository) {
        this.trainerPlanRepository = trainerPlanRepository;
    }

    @Override
    public Optional<TrainerPlan> findById(Long id) {
        return trainerPlanRepository.findById(id);
    }

    @Override
    public Optional<TrainerPlan> findByPlanTrainer_IdAndTrainerPlanUser_Id(Long trainerPlanId, Long userId) {
        return trainerPlanRepository.findByPlanTrainer_IdAndTrainerPlanUser_Id(trainerPlanId, userId);
    }

    @Override
    public Optional<TrainerPlan> findByIdAndTrainerPlanUser_Id(Long userId,Long trainerPlanId) {
        return trainerPlanRepository.findByIdAndTrainerPlanUser_Id(userId,trainerPlanId);
    }

    @Override
    public Optional<TrainerPlan> findByIdAndPlanTrainer_Id(Long id,Long trainerPlanId) {
        return trainerPlanRepository.findByIdAndPlanTrainer_Id(id,trainerPlanId);
    }

    @Override
    public List<TrainerPlan> findByPlanTrainer_Id(Long id) {
        return trainerPlanRepository.findByPlanTrainer_Id(id);
    }

    @Override
    public List<TrainerPlan> findByTrainerPlanUser_Id(Long id) {
        return trainerPlanRepository.findByTrainerPlanUser_Id(id);
    }

    @Override
    public boolean deleteById(Long id) {
        trainerPlanRepository.deleteById(id);

        return true;
    }

    @Override
    public boolean create(TrainerPlan trainerPlan) {
        trainerPlanRepository.save(trainerPlan);

        return true;
    }
}
