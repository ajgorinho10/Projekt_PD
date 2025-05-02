package projekt.PD.DataBase.DB_UserWorkout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface User_WorkoutsRepository extends JpaRepository<User_Workouts, Integer> {
    List<User_Workouts> findByUser_Id(int id);
    User_Workouts findById(int id);
    void deleteById(int id);
}
