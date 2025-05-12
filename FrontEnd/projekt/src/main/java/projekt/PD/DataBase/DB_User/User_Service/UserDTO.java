package projekt.PD.DataBase.DB_User.User_Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_UserWorkout.UserWorkout_Service.WorkoutDTO;
import projekt.PD.DataBase.DB_UserWorkout.User_Workouts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String first_name;
    private String last_name;
    private String roles;
    private String login;

    public UserDTO(User user) {
        this.id = user.getId();
        this.first_name = user.getFirstName();
        this.last_name = user.getLastName();
        this.roles = user.getRoles();
        this.login = user.getLogin();
    }

    public static List<UserDTO> toDTO(List<User> users) {
        List<UserDTO> userDTO = new ArrayList<>();
        for (User user : users) {
            userDTO.add(new UserDTO(user));
        }
        return userDTO;
    }
}
