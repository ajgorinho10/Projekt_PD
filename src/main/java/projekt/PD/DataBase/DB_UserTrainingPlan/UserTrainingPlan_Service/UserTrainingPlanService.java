package projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service;

import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;

import java.util.List;
import java.util.Optional;

public interface UserTrainingPlanService {
    Optional<UserTrainingPlan> findById(Long id,Long userId);
    List<UserTrainingPlan> findByUser_Id(Long id);
    boolean deleteById(Long id,Long userId);
    void create_or_change(UserTrainingPlan userTrainingPlan);
    boolean isUserTrainingPlan(Long id,Long user_id);
}
