package projekt.PD.Controller.Api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanDTO;
import projekt.PD.DataBase.DB_TrainerPlan.TrainerPlan_Service.TrainerPlanService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trainerPlan")
public class TrainerPlanRestController {

    private final TrainerPlanService trainerPlanService;
    private final UserService userService;

    public TrainerPlanRestController(TrainerPlanService trainerPlanService, UserService userService) {
        this.trainerPlanService = trainerPlanService;
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getTrainerPlan() {
        User user = getUserID();
        List<TrainerPlan> plans = trainerPlanService.findByTrainerPlanUser_Id(user.getId());
        if(plans!=null) {
            return new ResponseEntity<>(TrainerPlanDTO.toDTO(plans), HttpStatus.OK);
        }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserTrainingPlan(@PathVariable Long id) {
        User user = getUserID();
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndTrainerPlanUser_Id(id,user.getId());
        if(plan.isPresent()) {
            return new ResponseEntity<>(new TrainerPlanDTO(plan.get()), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUserTrainingPlan(@PathVariable Long id) {
        User user = getUserID();
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndTrainerPlanUser_Id(id,user.getId());
        if(plan.isPresent()) {
            trainerPlanService.deleteById(plan.get().getId());
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/trainer")
    public ResponseEntity<?> getPlanTrainer() {
        User user = getUserID();
        if(user.getTrainer()!=null) {
            List<TrainerPlan> plans = trainerPlanService.findByPlanTrainer_Id(user.getTrainer().getId());

            if(plans!=null) {
                return new ResponseEntity<>(TrainerPlanDTO.toDTO(plans), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/trainer/{id}")
    public ResponseEntity<?> getTrainerPlan(@PathVariable Long id) {
        User user = getUserID();
        if(user.getTrainer() != null) {
            Optional<TrainerPlan> plan = trainerPlanService.findByIdAndPlanTrainer_Id(id,user.getTrainer().getId());
            if(plan.isPresent()) {
                return new ResponseEntity<>(new TrainerPlanDTO(plan.get()), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/trainer/{id}")
    public ResponseEntity<?> createTrainerPlan(@ModelAttribute TrainerPlanDTO trainerPlanDTO,@PathVariable Long id) {
        User trainer = getUserID();
        User user = userService.findUserById(id);

        if(trainer.getTrainer() != null && user != null) {

            TrainerPlan trainerPlan = new TrainerPlan(trainerPlanDTO);
            trainerPlan.setPlanTrainer(trainer.getTrainer());
            trainerPlan.setTrainerPlanUser(user);

            if(trainerPlanService.create(trainerPlan)){
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @DeleteMapping("/trainer/{id}")
    public ResponseEntity<?> deleteTrainerPlan(@PathVariable Long id) {
        User user = getUserID();
        if(user.getTrainer() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<TrainerPlan> plan = trainerPlanService.findByIdAndPlanTrainer_Id(id,user.getTrainer().getId());

        if(plan.isPresent()) {
            trainerPlanService.deleteById(plan.get().getId());
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    //Funkcja pomocnicza do indentyfikowania uzytkownika
    private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }
}
