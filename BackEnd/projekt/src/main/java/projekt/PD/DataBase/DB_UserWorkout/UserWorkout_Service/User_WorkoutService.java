package projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service;

import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.List;
import java.util.Optional;

public interface User_WorkoutService {
    List<User_Workouts> findByUser_Id(Long id);
    Optional<User_Workouts> findById(Long id, Long user_id);
    boolean deleteById(Long id,Long user_id);
    void createUser_Workouts(User_Workouts user_workouts);
}
