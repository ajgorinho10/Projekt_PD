package projekt.PD.DataBase.DB_UserWorkout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Interfejs związanych z tabelą trainerrplan zapytań do bazy danych */

public interface User_WorkoutsRepository extends JpaRepository<User_Workouts, Long> {
    List<User_Workouts> findByUser_Id(Long user_id);
    Optional<User_Workouts> findById(Long id);
    void deleteById(Long id);
}
