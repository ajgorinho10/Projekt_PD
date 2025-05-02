package projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service;

import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.List;

public interface User_WorkoutService {
    List<User_Workouts> findByUser_Id(int id);
    User_Workouts findById(int id);
    void deleteById(int id);
    void createUser_Workouts(User_Workouts user_workouts);
}
