package projekt.PD.DataBase.DB_UserTrainingPlan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Interfejs metod związanych z zarządzaniem planami treningowymi użytkowników */

public interface UserTrainingPlanRepository extends JpaRepository<UserTrainingPlan, Long> {
    Optional<UserTrainingPlan> findById(Long id);
    boolean existsById(Long id);
    List<UserTrainingPlan> findByUser_Id(Long id);
    void deleteById(Long id);
}
