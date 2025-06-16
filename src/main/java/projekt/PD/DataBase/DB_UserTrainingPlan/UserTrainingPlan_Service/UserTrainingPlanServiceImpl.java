package projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan_Service;

import org.springframework.stereotype.Service;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlan;
import projekt.PD.DataBase.DB_UserTrainingPlan.UserTrainingPlanRepository;

import java.util.List;
import java.util.Optional;

/** Implementacja metod związanych z zarządzaniem planami treningowymi użytkowników */

@Service
public class UserTrainingPlanServiceImpl implements UserTrainingPlanService {

    private final UserTrainingPlanRepository userTrainingPlanRepository;

    public UserTrainingPlanServiceImpl(UserTrainingPlanRepository userTrainingPlanRepository) {
        this.userTrainingPlanRepository = userTrainingPlanRepository;
    }

    @Override
    public Optional<UserTrainingPlan> findById(Long id,Long userId) {
        if(isUserTrainingPlan(id,userId)) {
            return userTrainingPlanRepository.findById(id);
        }
        return Optional.empty();
    }

    @Override
    public List<UserTrainingPlan> findByUser_Id(Long id) {
        return userTrainingPlanRepository.findByUser_Id(id);
    }

    @Override
    public boolean deleteById(Long id,Long userId) {
        if(userTrainingPlanRepository.existsById(id)&&isUserTrainingPlan(id,userId)){

            userTrainingPlanRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public void create_or_change(UserTrainingPlan userTrainingPlan) {
        UserTrainingPlan utp = new UserTrainingPlan();

        utp.setUser(userTrainingPlan.getUser());
        utp.setTitle(userTrainingPlan.getTitle());
        utp.setDescription(userTrainingPlan.getDescription());
        utp.setMonday(userTrainingPlan.getMonday());
        utp.setTuesday(userTrainingPlan.getTuesday());
        utp.setWednesday(userTrainingPlan.getWednesday());
        utp.setThursday(userTrainingPlan.getThursday());
        utp.setFriday(userTrainingPlan.getFriday());
        utp.setSaturday(userTrainingPlan.getSaturday());
        utp.setSunday(userTrainingPlan.getSunday());

        if(userTrainingPlan.getId() == null){
            userTrainingPlanRepository.save(userTrainingPlan);
            return;
        }
        else{
            if(isUserTrainingPlan(userTrainingPlan.getId(),userTrainingPlan.getUser().getId())){
                utp.setId(userTrainingPlan.getId());
            }
        }

        userTrainingPlanRepository.save(utp);
    }

    @Override
    public boolean isUserTrainingPlan(Long id,Long user_id) {
        Optional<UserTrainingPlan> tmp = userTrainingPlanRepository.findById(id);
        return tmp.isPresent() && tmp.get().getUser().getId().equals(user_id);
    }
}
