package projekt.PD.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;


@Service
public class CurrentUser {
    private final UserService userService;

    @Autowired
    public CurrentUser(UserService userService) {
        this.userService = userService;
    }


    public User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }

    public void addUserToModel(Model model) {
        User user = getUserID();
        model.addAttribute("user", user);
    }
}
