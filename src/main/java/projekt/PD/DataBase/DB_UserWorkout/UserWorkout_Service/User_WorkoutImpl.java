package projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service;

import org.springframework.stereotype.Service;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;
import projekt.PD.DataBase.DB_UserWorkout.User_WorkoutsRepository;

import java.util.List;
import java.util.Optional;

/** Implementacja metod związanych z zarządzaniem treningami użytkowników */

@Service
public class User_WorkoutImpl implements User_WorkoutService {

    private final User_WorkoutsRepository user_workoutsRepository;

    public User_WorkoutImpl(User_WorkoutsRepository userWorkoutsRepository) {
        user_workoutsRepository = userWorkoutsRepository;
    }

    @Override
    public List<User_Workouts> findByUser_Id(Long id) {
        return user_workoutsRepository.findByUser_Id(id);
    }

    @Override
    public Optional<User_Workouts> findById(Long id, Long user_id) {
        if(isUserWorkout(id, user_id)){
            return user_workoutsRepository.findById(id);
        }

        return Optional.empty();
    }

    @Override
    public boolean deleteById(Long id,Long user_id) {
        if(isUserWorkout(id, user_id)) {
            user_workoutsRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public void createUser_Workouts(User_Workouts user_workouts) {

        if(user_workouts.getId() == null){
            user_workoutsRepository.save(user_workouts);
        }
        else{
            if(isUserWorkout(user_workouts.getId(), user_workouts.getUser().getId())) {
                user_workoutsRepository.save(user_workouts);
            }
            else{
                User_Workouts us = new User_Workouts();
                us.setUser(user_workouts.getUser());
                us.setDate(user_workouts.getDate());
                us.setTitle(user_workouts.getTitle());
                us.setDescription(user_workouts.getDescription());
                user_workoutsRepository.save(us);
            }
        }

    }

    private boolean isUserWorkout(Long id, Long user_id) {
        Optional<User_Workouts> workout = user_workoutsRepository.findById(id);
        return workout.isPresent() && workout.get().getUser().getId().equals(user_id);
    }
}
