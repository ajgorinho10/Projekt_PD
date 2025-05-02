package projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service;

import org.springframework.stereotype.Service;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;
import projekt.PD.DataBase.DB_UserWorkout.User_WorkoutsRepository;

import java.util.List;

@Service
public class User_WorkoutImpl implements User_WorkoutService {

    private final User_WorkoutsRepository user_workoutsRepository;

    public User_WorkoutImpl(User_WorkoutsRepository userWorkoutsRepository) {
        user_workoutsRepository = userWorkoutsRepository;
    }

    @Override
    public List<User_Workouts> findByUser_Id(int id) {
        return user_workoutsRepository.findByUser_Id(id);
    }

    @Override
    public User_Workouts findById(int id) {
        return user_workoutsRepository.findById(id);
    }

    @Override
    public void deleteById(int id) {
        user_workoutsRepository.deleteById(id);
    }

    @Override
    public void createUser_Workouts(User_Workouts user_workouts) {
        user_workoutsRepository.save(user_workouts);
    }
}
